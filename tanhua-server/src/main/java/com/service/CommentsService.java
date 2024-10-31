package com.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.dubbo.config.annotation.Reference;
import com.dubbo.api.QuanZiApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dubbo.pojo.Comment;
import com.pojo.User;
import com.pojo.UserInfo;
import com.pojo.vo.CommentsVO;
import com.dubbo.pojo.vo.PageInfo;
import com.pojo.vo.PageResult;
import com.utils.UserThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;


    /**
     * 查询评论s
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryCommentsList(String publishId, Integer page, Integer pagesize) {
        User user = UserThreadLocal.get();
        if(user == null){
            return null;
        }

        PageInfo<Comment> pageInfo = quanZiApi.queryCommentList(publishId,page,pagesize);
        List<Comment> records = pageInfo.getRecords();

        if (records.isEmpty()) {
            //没记录 （评论）
            PageResult pageResult = new PageResult();
            pageResult.setPage(page);
            pageResult.setPagesize(pagesize);
            pageResult.setPages(0);
            pageResult.setCounts(0);
            return pageResult;
        }

//组装pageResult  这里逻辑和查所有publish类似
    //前置工作
        //userId 查userInfo
        List<Long> userIds = new ArrayList<>();
        //result : commentsVO List
        List<CommentsVO> result = new ArrayList<>();
        //userinfo wrapper
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos =
                this.userInfoService.queryList(queryWrapper);


        //records : comments --> commentsVO
        for(Comment record : records){
            //result : commentsVO
            CommentsVO commentsVO = new CommentsVO();
            //组装userIds
            if (!userIds.contains(record.getUserId())) {
                userIds.add(record.getUserId());
            }
            //set record 相关
            commentsVO.setContent(record.getContent());
            commentsVO.setCreateDate(new
                    DateTime(record.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            commentsVO.setId(record.getId().toHexString());
            //组装 userinfo有关的
            for(UserInfo userInfo : userInfos){
                if (record.getUserId().longValue() ==
                        userInfo.getUserId().longValue()) {
                    commentsVO.setAvatar(userInfo.getLogo());
                    commentsVO.setNickname(userInfo.getNickName());
                    break;
                }
            }

            //redis          //还差自己是否点赞和点赞数

            //TODO (LikeCount)这点赞数也有问题吧，只能和不是publish的点赞数吗

            //HasLiked
            String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() +
                    "_" + commentsVO.getId();
            commentsVO.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 :
                    0); //是否点赞（1是，0否）


            //把每一个由comments转换成的commentsVO装进最后结果
            result.add(commentsVO);
        }
        //最后结果
        PageResult pageResult = new PageResult();
        pageResult.setItems(result);
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        //TODO 这包有问题啊
        pageResult.setPages(0);
        pageResult.setCounts(0);
        return pageResult;

    }

    /**
     * 保存评论  直接quanzi调save 这里可没mongo
     *
     * @param publishId
     * @param content
     * @return
     */
    public Boolean saveComments(String publishId, String content) {
        User user = UserThreadLocal.get();
        return this.quanZiApi.saveComment(user.getId(), publishId, 2,
                content);
    }
}
