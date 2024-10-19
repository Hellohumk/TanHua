package com.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.dubbo.config.annotation.Reference;

import com.api.QuanZiApi;
import com.api.UsersApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pojo.*;
import com.pojo.vo.*;
import com.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class IMService {

    @Reference(version = "1.0.0")
    private UsersApi usersApi;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private AnnouncementService announcementService;



    @Value("${tanhua.sso.url}")
    private String url;

    /**
     * 添加联系人 （表＋环信）
     * @param userId
     * @return
     */
    public boolean contactUser(Long userId) {
        User user = UserThreadLocal.get();

        Users users = new Users();
        users.setUserId(user.getId());
        users.setFriendId(userId);

        //mongo中存入
        String id = usersApi.saveUsers(users);
        if(StringUtils.isNotEmpty(id)){
            //success 注册关系（和sso中环信相同，url不同）
            String targetUrl = url + "/user/huanxin/contacts/" +
                    users.getUserId() + "/" + users.getFriendId();
            ResponseEntity<Void> responseEntity =
                    this.restTemplate.postForEntity(targetUrl, null, Void.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 史上最傻逼业务
     *
     * 查找联系人s（名字 or 分页）
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    public PageResult queryContactsList(Integer page, Integer pageSize, String keyword) {
        User user = UserThreadLocal.get();

        List<Users> usersList = null;

        //这里的查询是为了和后面userinfo查出来的一一对应，故这里做出的决策是：宁大勿小
        //给关键字就不能分页，就给全部；分了页就给这一页就行
        //keywords是名字的关键词

        //为什么不全部搞清楚再准确查找id，因为分布式封装后只有下面两个查找函数。没得挑除非再加一个api，但是没做到复用
        if (StringUtils.isNotEmpty(keyword)) {
            //keywords不为空 则全部查出
            usersList = this.usersApi.queryAllUsersList(user.getId());
        } else {
            //空则按分页查询
            PageInfo<Users> usersPageInfo =
                    this.usersApi.queryUsersList(user.getId(), page, pageSize);
            usersList = usersPageInfo.getRecords();
        }

        //UserInfo 那一套
        List<Long> userIds = new ArrayList<>();
        for (Users users : usersList) {
            userIds.add(users.getFriendId());
        }
        //mp 查询 每个id对应的userInfo
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);

        //有关键字就给查，没有就不给查
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("nick_name", keyword);
        }
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);

        //result创建
        List<Contacts> contactsList = new ArrayList<>();

        if(StringUtils.isEmpty(keyword)){
            for (Users users : usersList) {
                for (UserInfo userInfo : userInfoList) {
                    if (users.getFriendId().longValue() ==
                            userInfo.getUserId().longValue()) {
                        //一一对应，userList是大的，userInfo的列表框定了最后需要的（名字相似的）
                        Contacts contacts = new Contacts();
                        contacts.setAge(userInfo.getAge());
                        contacts.setAvatar(userInfo.getLogo());
                        contacts.setNickname(userInfo.getNickName());
                        contacts.setGender(userInfo.getSex().name().toLowerCase());
                        contacts.setCity(StringUtils.substringBefore(userInfo.getCity(),"-"));
                        //存时LOng ，返回给String
                        contacts.setUserId(String.valueOf(userInfo.getUserId()));
                        contactsList.add(contacts);
                        break;
                    }
                }
            }
        }else{
            //这里的userlIst就框定了结果，因为分页查询
            for (UserInfo userInfo : userInfoList) {
                Contacts contacts = new Contacts();
                contacts.setAge(userInfo.getAge());
                contacts.setAvatar(userInfo.getLogo());
                contacts.setGender(userInfo.getSex().name().toLowerCase());
                contacts.setNickname(userInfo.getNickName());
                contacts.setUserId(String.valueOf(userInfo.getUserId()));
                contacts.setCity(StringUtils.substringBefore(userInfo.getCity(), "-"));
                contactsList.add(contacts);
            }
        }
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPagesize(pageSize);
        pageResult.setItems(contactsList);
        return pageResult;

    }

    //列表查询 ： 喜欢 点赞 评论
    public PageResult queryMessageLikeList(Integer page, Integer pageSize) {
        return this.messageCommentList(1, page, pageSize);
    }
    public PageResult queryMessageCommentList(Integer page, Integer
            pageSize) {
        return this.messageCommentList(2, page, pageSize);
    }
    public PageResult queryMessageLoveList(Integer page, Integer pageSize)
    {
        return this.messageCommentList(3, page, pageSize);
    }

    private PageResult messageCommentList(Integer type, Integer page,
                                          Integer pageSize) {
        User user = UserThreadLocal.get();
        PageInfo<Comment> pageInfo = quanZiApi.queryCommentListByUser(user.getId(),type,page,pageSize);

        //封装
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        //TODO 有问题
        pageResult.setPages(0);
        pageResult.setCounts(0);

        pageResult.setPagesize(pageSize);
        //取出records： 将comment -》 messageLike
        //这里他妈的也是可笑，相当于Comments我只要了个id属性，即拿到是谁comment了我，然后再补全对应人的信息。comment具体是什么无所吊慰
        //最后前端显示的是 ：    xxx comment了我~
        List<Comment> records = pageInfo.getRecords();

        List<Long> userIds = new ArrayList<>();
        for(Comment comment : records){
            userIds.add(comment.getUserId());
        }

        //查对应userInfo 、、TODO 试着抽离一下
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);

        //拼接
        List<MessageLike> messageLikeList = new ArrayList<>();
        for (Comment record : records) {
            for (UserInfo userInfo : userInfoList) {
                if (userInfo.getUserId().longValue() ==
                        record.getUserId().longValue()) {
                    //一一对应
                    MessageLike messageLike = new MessageLike();
                    messageLike.setId(record.getId().toHexString());
                    messageLike.setAvatar(userInfo.getLogo());
                    messageLike.setNickname(userInfo.getNickName());
                    messageLike.setCreateDate(new
                            DateTime(record.getCreated()).toString("yyyy-MM-dd HH:mm"));
                    messageLikeList.add(messageLike);
                    break;
                }
            }
        }
        pageResult.setItems(messageLikeList);
        return pageResult;

    }


    public PageResult queryMessageAnnouncementList(Integer page, Integer pageSize) {
        IPage<Announcement> announcementPage =
                this.announcementService.queryList(page, pageSize);

        //result
        List<MessageAnnouncement> messageAnnouncementList = new ArrayList<>();

        //赋值
        for (Announcement record : announcementPage.getRecords()) {
            MessageAnnouncement messageAnnouncement = new
                    MessageAnnouncement();
            messageAnnouncement.setId(record.getId().toString());
            messageAnnouncement.setTitle(record.getTitle());
            messageAnnouncement.setDescription(record.getDescription());
            messageAnnouncement.setCreateDate(new
                    DateTime(record.getCreated()).toString("yyyy-MM-dd HH:mm"));
            messageAnnouncementList.add(messageAnnouncement);
        }
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        //TODO 有问题
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPagesize(pageSize);
        pageResult.setItems(messageAnnouncementList);
        return pageResult;

    }
}
