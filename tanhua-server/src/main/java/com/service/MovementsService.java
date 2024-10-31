package com.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dubbo.api.QuanZiApi;
import com.dubbo.api.VisitorsApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dubbo.pojo.Publish;
import com.dubbo.pojo.vo.PageInfo;
import com.pojo.User;
import com.pojo.UserInfo;
import com.dubbo.pojo.Visitors;
import com.pojo.vo.Movements;
import com.pojo.vo.PageResult;
import com.pojo.vo.PicUploadResult;
import com.pojo.vo.VisitorsVo;
import com.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;


/*
TODO 记得导包 aliyun 和他的config
 */

@Service
public class MovementsService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private UserService userService;

    @Autowired
    private PicUploadService picUploadService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 这个是圈子上传  dubbo服务中做的是，补全publish（这个service也可以实现），然后三个表的插入（包括publish）
     *
     * 三个表的插入在dubbo我可以理解（数据库不在这），为什么这里要赋值publish
     *
     * @param textContent
     * @param location
     * @param multipartFile
     * @param token
     * @return
     */
    public String savePublish(String textContent,String longitude,String latitude, String location, MultipartFile[] multipartFile, String token) {

        //查询当前的登录信息
        User user = userService.queryUserByToken(token);
        if (null == user) {
            return null;
        }
        //也可以是： User user = UserThreadLocal.get();

        Publish publish =new Publish();
        publish.setUserId(user.getId());
        publish.setText(textContent);
        publish.setLocationName(location);
        //where?
        publish.setLatitude(latitude);
        publish.setLongitude(longitude);
        //????????????????????? 默认吗
        publish.setSeeType(1);

        List<String> picUrls = new ArrayList<>();
        //upload
        for(MultipartFile file : multipartFile) {
            PicUploadResult picUploadResult = picUploadService.upload(file);
            picUrls.add(picUploadResult.getName()); // 拿到对应的存储地址
        }

        //好吧 理解为什么publish要在这了，但前面是不是有点多余？
        publish.setMedias(picUrls);
        quanZiApi.savePublish(publish);

        return publish.getId().toString();

    }

    /**
     * 第三个参数为是否查询推荐  ，  这里函数为，查询时间线即改任对应要展示的圈子
     * @param page
     * @param pageSize
     * @param isRecommend
     * @return
     */
    public PageResult queryPublishList(Integer page, Integer pageSize, Boolean isRecommend) {
        //User
        User user = UserThreadLocal.get();

        //true : null --> 给进去id为null，触发插叙推荐列表
        Long userId = isRecommend ? null  : user.getId();


        //拿到对应角色所应该展示的publish
        PageInfo<Publish> pageInfo = quanZiApi.queryPublishList(userId,page,pageSize);
        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        pageResult.setCounts(0);//总数
        pageResult.setPages(0);//共有几页

        //取出list of publish
        List<Publish> records = pageInfo.getRecords();
        if (records.isEmpty()) {
//没有动态信息
            return pageResult;//即records == null
        }

        //返回给前端是movements，现封装
        List<Movements> movementsList = new ArrayList<>();
        //拿到对应publish（movement）的发布者id
        List<Long> userIds = new ArrayList<>();
        for (Publish record : records) {
            Movements movements = new Movements();
            //两个id
            movements.setId(record.getId().toHexString());
            movements.setUserId(record.getUserId());
            //id单独拿出，后面要查userinfo
            if(!userIds.contains(movements.getId())){
                //务必检查，null就死了
                userIds.add(movements.getUserId());
            }
            //其余
            movements.setImageContent(record.getMedias().toArray(new
                    String[]{}));
            movements.setTextContent(record.getText());

            LocalDateTime createdTime = LocalDateTime.ofInstant(new Date(record.getCreated()).toInstant(), ZoneId.systemDefault());
            String relativeDate = createdTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) + "秒前";
            movements.setCreateDate(relativeDate);

            //LikeCount and HasLiked
            String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + movements.getId();
            movements.setHasLiked(redisTemplate.hasKey(userKey) ? 1 : 0);

            String key = "QUAZI_COMMENT_LIKE_" + movements.getId();
            String value = (String) redisTemplate.opsForValue().get(key);
            if(StringUtils.isNotEmpty(value)){
                movements.setLikeCount(Integer.valueOf(value));
            }else{
                //空 即没有记录，没记录说明就没人点赞
                movements.setLikeCount(0);

            }




            //TODO 这是还未实现的！
            movements.setLoveCount(100);//喜欢
            movements.setDistance("1.2km");//distance
            movements.setCommentCount(30);//评论


            movementsList.add(movements);
        }





        //拿到对应着作者的详细信息：目的填充字段：age avater gender nickname tags
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryList(queryWrapper);

        //一一对应
        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfos) {
                if (movements.getUserId().longValue() ==
                        userInfo.getUserId().longValue()) {
                    //set
                    movements.setAge(userInfo.getAge());
                    movements.setAvatar(userInfo.getLogo());
                    movements.setNickname(userInfo.getNickName());
                    movements.setTags(StringUtils.split(userInfo.getTags(),','));//标签数组
                    movements.setGender(userInfo.getSex().name().toLowerCase());//name是拿到我定义的数字对应的字符串，然后把他全小写化，所以2的话这里返回的是woman

                    break;//不用找了
                }
            }
        }

        pageResult.setItems(movementsList);
        return pageResult;


    }

    //redis里记录用户是否对这个视频点赞，是为了圈子中是否显示是否已经点赞，这个是这样存储的

    /**
     * 点赞
     * @param publishId
     * @return
     */
    public Long likeComment(String publishId) {
        User user = UserThreadLocal.get();
        //这里已经点赞数++了，这个点赞数的改变来自comment表的插入
        boolean bool = quanZiApi.saveLikeComment(user.getId(),publishId);
        if(!bool){
            return null;//failed 从api到这为 ： false --> null ---> error输出
        }

        Long likeCount = 0L;

        //保存点赞数到redis
        String key = "QUANZI_COMMENT_LIKE_" + publishId;
        if (!this.redisTemplate.hasKey(key)) {
            //redis没有该publish的点赞记录的话就查询目前总点赞数，存入redis
            Long count = this.quanZiApi.queryCommentCount(publishId, 1);
            likeCount = count;
            this.redisTemplate.opsForValue().set(key,
                    String.valueOf(likeCount));
        } else {
            //redis有的话就让记录+1，让redis数据保持同步一致性
            likeCount = this.redisTemplate.opsForValue().increment(key);

        }

        //记录已点赞  、、记录谁点了赞
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" +
                publishId;
        this.redisTemplate.opsForValue().set(userKey, "1");
        return likeCount;

    }


    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    public Long cancelLikeComment(String publishId){
        User user = UserThreadLocal.get();
        boolean bool = quanZiApi.removeComment(user.getId(), publishId,1);
        if(! bool) {
            return null;//failed
        }
        //redis
        String key = "QUANZI_COMMENT_LIKE_" + publishId;
        Long likeCount = redisTemplate.opsForValue().decrement(key);
        //redis 用户行为记录删除
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" +
                publishId;
        redisTemplate.delete(userKey);

        return likeCount;

    }

    /**
     * 喜欢
     * @param publishId
     * @return
     */

    public Long loveComment(String publishId) {
        User user = UserThreadLocal.get();
        //这里已经点赞数++了，这个点赞数的改变来自comment表的插入
        boolean bool = quanZiApi.saveLoveComment(user.getId(),publishId);
        if(!bool){
            return null;//failed 从api到这为 ： false --> null ---> error输出
        }

        Long loveCount = 0L;

        //保存喜欢数到redis
        String key = "QUANZI_COMMENT_LOVE_" + publishId;
        if (!this.redisTemplate.hasKey(key)) {
            //redis没有该publish的喜欢记录的话就查询目前总喜欢数，存入redis
            Long count = this.quanZiApi.queryCommentCount(publishId, 3);
            loveCount = count;
            this.redisTemplate.opsForValue().set(key,
                    String.valueOf(loveCount));
        } else {
            //redis有的话就让记录+1，让redis数据保持同步一致性
            loveCount = this.redisTemplate.opsForValue().increment(key);

        }

        //记录已点赞  、、记录谁点了赞
        String userKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" +
                publishId;
        this.redisTemplate.opsForValue().set(userKey, "3"); //这里是随便丢进去一个value，这里是3，就是存一下是否存在记录就行
        return loveCount;
    }

    /**
     * 取消喜欢
     * @param publishId
     * @return
     */
    public Long cancelLoveComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean bool = quanZiApi.removeComment(user.getId(), publishId,3);
        if(! bool) {
            return null;//failed
        }
        //redis
        String key = "QUANZI_COMMENT_LOVE_" + publishId;
        Long loveCount = redisTemplate.opsForValue().decrement(key);//减少1并返回
        //redis 用户行为记录删除
        String userKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" +
                publishId;
        redisTemplate.delete(userKey);

        return loveCount;
    }

    public Movements queryById(String publishId) {
        Publish publish = this.quanZiApi.queryPublishById(publishId);
        if (null == publish) {
            return null;
        }

        //构建movement基础设置
        Movements movements = new Movements();
        movements.setId(publish.getId().toHexString());
        movements.setImageContent(publish.getMedias().toArray(new String[] {} ));//List 转 array
        movements.setTextContent(publish.getText());
        movements.setUserId(publish.getUserId());
        LocalDateTime createdTime = LocalDateTime.ofInstant(new Date(publish.getCreated()).toInstant(), ZoneId.systemDefault());
        String relativeDate = createdTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) + "秒前";
        movements.setCreateDate(relativeDate);

        //useriinfo 补全 movement
        UserInfo userInfo =
                this.userInfoService.queryById(publish.getUserId());
        if (null == userInfo) {
            return null;
        }

        //上面queryList是多个，这个是单个的

        movements.setAge(userInfo.getAge());
        movements.setAvatar(userInfo.getLogo());
        movements.setGender(userInfo.getSex().name().toLowerCase());
        movements.setNickname(userInfo.getNickName());
        movements.setTags(StringUtils.split(userInfo.getTags(), ','));
        movements.setCommentCount(10); //TODO 评论数
        movements.setDistance("1.2公里"); //TODO 距离
        //Like and HasLiked
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + userInfo.getUserId()
                + "_" + movements.getId();
        movements.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 : 0);
        //是否点赞（1是，0否）

        String key = "QUANZI_COMMENT_LIKE_" + movements.getId();
        String value = (String) this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(value)) {
            movements.setLikeCount(Integer.valueOf(value)); //点赞数
        } else {
            movements.setLikeCount(0);
        }

        //Love and HasLoved
        String userLoveKey = "QUANZI_COMMENT_LOVE_USER_" +
                userInfo.getUserId() + "_" + movements.getId();
        movements.setHasLoved(this.redisTemplate.hasKey(userLoveKey) ? 1 :
                0); //是否喜欢（1是，0否）
        key = "QUANZI_COMMENT_LOVE_" + movements.getId();
        value = (String) this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(value)) {
            movements.setLoveCount(Integer.valueOf(value)); //喜欢数
        } else {
            movements.setLoveCount(0);
        }

        return movements;

    }


    //day09 谁看过我

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    public List<VisitorsVo> queryVisitorsList() {
        User user = UserThreadLocal.get();

        if(user == null){
            return  null;
        }

        //TODO 什么🖊逻辑，那也应该是date + userid双主键啊
        //这个是操做value 存的是上一次查看的时间
        String redisKey = "visitor_date_" + user.getId();

        List<Visitors> visitorsList = null;
        String value = (String) this.redisTemplate.opsForValue().get(redisKey);

        //这里的逻辑是：
        // 如果redis没保存上次我查看的时间（即我一次都没查过看过的人）那么我就查看过我的人的前5个
        // 如果redis保存了上次我查看的时间，就顺着这个时间往后查看过我的人
        if(StringUtils.isEmpty(value)){
            visitorsList = this.visitorsApi.topVisitor(user.getId(), 5);
        }else{
            visitorsList = this.visitorsApi.topVisitor(user.getId(),
                    Long.valueOf(value));

            //TODO 这里不用更新redis吗？
        }

        if(visitorsList.isEmpty()){
            //没人看过我 666
            return Collections.emptyList();
        }

        //classic 补全个人信息封装
        List<Long> userIds = new ArrayList<>();
        for (Visitors visitor : visitorsList) {
            userIds.add(visitor.getVisitorUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);
        //result
        List<VisitorsVo> visitorsVoList = new ArrayList<>();
        //id匹配加封装
        for(Visitors visitor : visitorsList){
            for(UserInfo userInfo : userInfoList){
                if(visitor.getVisitorUserId().longValue() == userInfo.getUserId().longValue()){
                    VisitorsVo visitorsVo = new VisitorsVo();
                    visitorsVo.setAge(userInfo.getAge());
                    visitorsVo.setAvatar(userInfo.getLogo());
                    visitorsVo.setGender(userInfo.getSex().name().toLowerCase());
                    visitorsVo.setId(userInfo.getUserId());
                    visitorsVo.setNickname(userInfo.getNickName());
                    visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
                    visitorsVo.setFateValue(visitor.getScore().intValue());
                    visitorsVoList.add(visitorsVo);
                    break;
                }
            }
        }

        return visitorsVoList;

    }

    public PageResult queryAlbumList(Long userId, Integer page, Integer pageSize) {

        //result
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        PageInfo<Publish> pageInfo = this.quanZiApi.queryAlbumList(userId,page, pageSize);

        List<Publish> records = pageInfo.getRecords();//拿到所有的发布publish

        if(records == null){
            return pageResult;
        }
        // 开始封装 movements
        List<Movements> movementsList = new ArrayList<>();
        for(Publish record : records){
            Movements movements = new Movements();
            movements.setId(record.getId().toHexString());
            movements.setImageContent(record.getMedias().toArray(new
                    String[]{}));
            movements.setTextContent(record.getText());
            movements.setUserId(record.getUserId());
            //setCreateDate
            LocalDateTime createdTime = LocalDateTime.ofInstant(new Date(record.getCreated()).toInstant(), ZoneId.systemDefault());
            String relativeDate = createdTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) + "秒前";
            movements.setCreateDate(relativeDate);

            movementsList.add(movements);
        }

        //classic 拿id查userinfo + 一一对应封装
        List<Long> userIds = new ArrayList<>();
        for (Movements movements : movementsList) {
            if (!userIds.contains(movements.getUserId())) {
                userIds.add(movements.getUserId());
            }
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos =
                this.userInfoService.queryList(queryWrapper);
        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfos) {
                if (movements.getUserId().longValue() ==
                        userInfo.getUserId().longValue()) {
                    //set
                    movements.setAge(userInfo.getAge());
                    movements.setAvatar(userInfo.getLogo());
                    movements.setNickname(userInfo.getNickName());
                    movements.setTags(StringUtils.split(userInfo.getTags(), ','));//标签数组
                    movements.setGender(userInfo.getSex().name().toLowerCase());//name是拿到我定义的数字对应的字符串，然后把他全小写化，所以2的话这里返回的是woman

                    break;//不用找了
                }
            }
        }
        pageResult.setItems(movementsList);
        return pageResult;
    }
}
