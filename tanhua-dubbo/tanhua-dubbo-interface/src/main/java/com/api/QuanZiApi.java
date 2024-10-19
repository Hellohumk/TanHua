package com.api;

import com.pojo.Comment;
import com.pojo.Publish;
import com.pojo.vo.PageInfo;

public interface QuanZiApi {
    /**
     * 发布动态
     *
     * @param publish
     * @return
     */
    boolean savePublish(Publish publish);

    /**
     *
     * 查询动态s  timeline表 这个是自己应该展示的圈子
     *
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Publish> queryPublishList(Long userId,Integer page,Integer pageSize);

    /**
     * 点赞
     *
     * @param userId
     * @param publishId
     * @return
     */
    boolean saveLikeComment(Long userId, String publishId);

    /**
     * 取消对应帖子的喜欢 or 点赞 Ping论
     *
     * y?这里的设计无需考虑controller具体业务，讲究模块通用性，复用性就可以。要能被多个人用
     *
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    boolean removeComment(Long userId, String publishId, Integer
            commentType);

    /**
     * 喜欢
     *
     * @param userId
     * @param publishId
     * @return
     */
    boolean saveLoveComment(Long userId, String publishId);

    /**
     * 保存评论 （这里的评论是喜欢 点赞 和评论都从这里走）
     *
     * @param userId
     * @param publishId
     * @param type
     * @param content
     * @return
     */
    boolean saveComment(Long userId, String publishId, Integer type, String
            content);


    /**
     * 查询对应帖子的 评论 or 喜欢 or 点赞
     *
     * @param publishId
     * @param type
     * @return
     */
    Long queryCommentCount(String publishId, Integer type);

    /**
     * 根据id查询动态
     * @param id
     * @return
     */
    Publish queryPublishById(String id);

    PageInfo<Comment> queryCommentList(String publishId,Integer page,Integer pageSize);

    /**
     * 查询用户的评论数据
     * 通过userId 拿回对应type的消息列表
     *
     * @return
     */
    PageInfo<Comment> queryCommentListByUser(Long userId, Integer type,
                                             Integer page, Integer pageSize);

    /**
     * 查询自己发布的publish   album表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Publish> queryAlbumList(Long userId, Integer page, Integer
            pageSize);

}
