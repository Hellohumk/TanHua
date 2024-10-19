package com.service;


import com.alibaba.dubbo.config.annotation.Reference;
import com.api.UserLikeApi;
import com.api.UserLocationApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enums.SexEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.Question;
import com.pojo.RecommendUser;
import com.pojo.User;
import com.pojo.UserInfo;
import com.pojo.dto.RecommendUserQueryParam;
import com.pojo.vo.*;
import com.utils.UserThreadLocal;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TodayBestService {

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    @Value("${tanhua.sso.default.user}") //2
    private Long defaultUser;

    @Autowired
    private RestTemplate restTemplate;

    //day09 默认推荐user
    @Value("${tanhua.sso.default.recommend.users}")
    private String defaultRecommendUsers;

    @Value("${tanhua.sso.url}")
    private String url;//sso服务器url

    private ObjectMapper MAPPER = new ObjectMapper();


    public TodayBest queryTodayBest(String token){
        //1 根据token查用户
        User user = userService.queryUserByToken(token);
        if(user == null){
            //没这个几把
            return null;
        }
        //2 查询今日佳人
        TodayBest todayBest = recommendUserService.queryTodayBest(user.getId());

        //封装 还没封装完的，还差userinfo相关的东西
        Long targetId = null;

        if(todayBest == null){
            //没有匹配的
            todayBest = new TodayBest();

            //好SB的逻辑 TODO 后面加geo可以改一下
            targetId = defaultUser; //设置Id为默认者

        }else{
            targetId = todayBest.getId();
        }
        UserInfo userInfo = this.userInfoService.queryById(targetId);
        todayBest.setId(targetId);//20241013 别觉得这里傻逼，我改的，因为之前更是狗屎
        todayBest.setAge(userInfo.getAge());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setGender(userInfo.getSex().toString());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

        return todayBest;
    }

    //day09 多态  差的也是今日最佳，但是给进来的就是最佳的id
    public TodayBest queryTodayBest(Long userId){
        User user = UserThreadLocal.get();
        TodayBest todayBest = new TodayBest();
        //补全信息
        UserInfo userInfo = this.userInfoService.queryById(user.getId());
        todayBest.setAge(userInfo.getAge());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setGender(userInfo.getSex().toString());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

        return todayBest;

    }

    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam, String token) {
        //查询当前的登录信息
        User user = this.userService.queryUserByToken(token);
        if (null == user) {
            return null;
        }

        PageResult pageResult = new PageResult();

        //pageinfo(佳人分页展示实现的service(封装))
        //这里pageinfo是 推荐的佳人的分页查询的封装结果 见interface模块下pojo
        PageInfo<RecommendUser> pageInfo = recommendUserService.queryRecommendUserList(user.getId(),queryParam.getPage(),queryParam.getPagesize());

        pageResult.setCounts(0); //前端不参与计算，仅需要返回字段 TODO 这里不查一下算一下？
        pageResult.setPage(queryParam.getPage());
        pageResult.setPagesize(queryParam.getPagesize());

        //最后结果要返回的是todayBest的List的PageResult
        List<RecommendUser> records = pageInfo.getRecords();


        // 判断record day09
        //如果没查到，就是用推荐列表
        if(CollectionUtils.isEmpty(records)){
            String[] ss = StringUtils.split(defaultRecommendUsers,",");

            for(String s : ss){
                RecommendUser recommendUser= new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());//推荐给当前用户
                recommendUser.setScore(RandomUtils.nextDouble(60,90));//本身就是自动推荐的，故随机产生得分

                records.add(recommendUser);
            }
        }


        //所有id进List
        List<Long> userIds = new ArrayList<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }

        //mp   查询UserInfo ，把对应的TodayBest的补全
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);


        //这里确实是可选的： 通过判断传回的queryParam是否为空，查看对应条件
        //异性
        if(StringUtils.isNotEmpty(queryParam.getGender())){
            queryWrapper.eq("sex", queryParam.getGender().equals("man") ? 1 : 2);
        }
        //同城
        if (StringUtils.isNotEmpty(queryParam.getCity())) { //居住城市
            queryWrapper.eq("city", queryParam.getCity());
        }
        //为什么一定是妹妹？ 0928
        if (queryParam.getAge() != null) { //年龄
            queryWrapper.lt("age", queryParam.getAge());
        }

        //找到符合要求的异性们 并且是推荐的
        List<UserInfo> userInfos =
                userInfoService.queryList(queryWrapper);

        //封装到TodayBest的List当中，最后set进pageResult的item中，作为结果输出
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            TodayBest todayBest = new TodayBest();
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

            //设置缘分值
            for (RecommendUser record : records) {
                //好煞笔啊  userinfo里没有分数，RecommendUser里有，逐个扫描配对，找到对应的RecommendUser
                if (userInfo.getUserId().longValue() ==
                        record.getUserId().longValue()) {
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                    break;
                }
            }

            todayBests.add(todayBest);
        }
        // 按缘分值倒叙排列
        Collections.sort(todayBests, ((o1, o2) -> (int) (o2.getFateValue() - o1.getFateValue())));


        pageResult.setItems(todayBests);

        //TODO 这里包有问题啊牢底。为什么筛选条件不改变查询页数 即counts --- page
        return pageResult;


    }

    @Autowired
    private QuestionService questionService;

    /**
     * 根据id拿到question的txt内容
     * @param userId
     * @return
     */
    public String queryQuestion(Long userId) {
        Question question = this.questionService.queryQuestion(userId);
        if (null != question) {
            return question.getTxt();
        }
        return "";
    }

    /**
     * 回答问题
     * @param userId
     * @param reply
     * @return
     */
    public Boolean replyQuestion(Long userId, String reply) {
        User user = UserThreadLocal.get();
        if(user == null){
            return false;
        }

        UserInfo userInfo = userInfoService.queryById(user.getId());//拿自己详细信息

        //构建消息内容
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId",user.getId().toString());
        msg.put("nickname",userInfo.getNickName());
        msg.put("strangerQuestion",queryQuestion(userId));

        try{
            String msgStr = MAPPER.writeValueAsString(msg);

            HttpHeaders headers = new HttpHeaders();
            /*
                headers 是一个代表HTTP响应头部的对象。
                setContentType 是一个方法，用于设置响应的Content-Type头部。
                MediaType.APPLICATION_FORM_URLENCODED 是一个枚举值，代表application/x-www-form-urlencoded这种媒体类型。 (告知这是个表单)
             */
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            //相对于普通的Map 他的值可以是List or set
            MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
            params.add("target", userId.toString());
            params.add("msg", msgStr);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            //调用sso上部署的sendMsg方法在HuanXinController上
            //这里tm傻逼的地方在为什么要架设在sso上，sso的sendMsg也就是换个url再发给环信第三方，参数变都不变
            //我认为最优解法是直接就在这里使用restTemplate给环信发消息即可，但是他又不是huanxinService，看自己项目怎么安排吧
            String targetUrl = url + "/user/huanxin/messages";

            ResponseEntity responseEntity = this.restTemplate.postForEntity(targetUrl, httpEntity, Void.class);

            return responseEntity.getStatusCodeValue() == 200;

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }

    //day 10
    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    public List<NearUserVo> queryNearUser(String gender, String distance) {
        User user = UserThreadLocal.get();
        if (user == null) {
            return null;
        }
        UserLocationVo userLocationVo = userLocationApi.queryByUserId(user.getId());
        Double longitude = userLocationVo.getLongitude();
        Double latitude = userLocationVo.getLatitude();

        //查附近的bitch
        List<UserLocationVo> userLocationList = this.userLocationApi.queryUserFromLocation(longitude, latitude, Integer.valueOf(distance));

        //附近没有bitch
        if (CollectionUtils.isEmpty(userLocationList)) {
            return Collections.emptyList();
        }

        //classtic  查询userinfo 补全bitch信息
        List<Long> userIds = new ArrayList<>();
        for (UserLocationVo locationVo : userLocationList) {
            userIds.add(locationVo.getUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        //根据gender设置性别区分
        if (StringUtils.equalsIgnoreCase(gender, "man")) {
            queryWrapper.in("sex", SexEnum.MAN);
        } else if (StringUtils.equalsIgnoreCase(gender, "woman")) {
            queryWrapper.in("sex", SexEnum.WOMAN);
        }
        List<UserInfo> userInfoList =
                this.userInfoService.queryList(queryWrapper);

        //result 封装 userLocationList --> nearUserVoList
        List<NearUserVo> nearUserVoList = new ArrayList<>();

        for (UserLocationVo locationVo : userLocationList) {

            // 排除自己搜的时候会把自己也搜到，自己也在园圈里
            if (locationVo.getUserId().longValue() ==
                    user.getId().longValue()) {
                continue;
            }

            for (UserInfo userInfo : userInfoList) {
                if (locationVo.getUserId().longValue() ==
                        userInfo.getUserId().longValue()) {
                    NearUserVo nearUserVo = new NearUserVo();
                    nearUserVo.setUserId(userInfo.getUserId());
                    nearUserVo.setAvatar(userInfo.getLogo());
                    nearUserVo.setNickname(userInfo.getNickName());

                    //add
                    nearUserVoList.add(nearUserVo);
                    break;

                }
            }
        }

        return nearUserVoList;
    }

    //tanhua
    public List<TodayBest> queryCardList() {
        User user = UserThreadLocal.get();
        if(user == null){
            return null;
        }

        int count = 50;//默认50个bitch
        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), 1, count);//查询所有佳人，一页50个，第一页的（评分最高的50个）

        //lol 你没有推荐的
        if (CollectionUtils.isEmpty(pageInfo.getRecords())) {
//默认推荐列表
            String[] ss = StringUtils.split(defaultRecommendUsers, ',');
            for (String s : ss) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());
                pageInfo.getRecords().add(recommendUser);
            }
        }

        //拿出这50个推荐人（也可能不足50）
        List<RecommendUser> records = pageInfo.getRecords();
        //随机挑 <= 10 人
        int showCount = Math.min(10, records.size());
        List<RecommendUser> newRecords = new ArrayList<>();
        for (int i = 0; i < showCount; i++) {
            //随机选出推荐的好友
            newRecords.add(records.get(RandomUtils.nextInt(0, records.size() - 1)));
        }

        //classic 为newRecords里面的人加userinfo
        List<Long> userIds = new ArrayList<>();
        for (RecommendUser record : newRecords) {
            userIds.add(record.getUserId());
        }
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryList(queryWrapper);

        //result : List<RecommendUser> (这里他只贡献了一组id，故无需双for) --> List<TodayBest>
        List<TodayBest> todayBests = new ArrayList<>();

        for (UserInfo userInfo : userInfos) {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

            //设置缘分值
            for (RecommendUser record : newRecords) {
                //好煞笔啊  userinfo里没有分数，RecommendUser里有，逐个扫描配对，找到对应的RecommendUser
                if (userInfo.getUserId().longValue() ==
                        record.getUserId().longValue()) {
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                    break;
                }
            }

            todayBests.add(todayBest);
        }

        return todayBests;

    }

    //day 10 用户喜欢
    @Autowired
    private IMService imService;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    public Boolean likeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        String id = this.userLikeApi.saveUserLike(user.getId(),
                likeUserId);
        if (StringUtils.isEmpty(id)) {
            return false;
        }
        if (this.userLikeApi.isMutualLike(user.getId(), likeUserId)) {
//相互喜欢成为好友
            this.imService.contactUser(likeUserId);
        }
        return true;

    }

    public Boolean disLikeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        return this.userLikeApi.deleteUserLike(user.getId(), likeUserId);

        //TODO 取消喜欢难道不可以删除好友？
    }
}
