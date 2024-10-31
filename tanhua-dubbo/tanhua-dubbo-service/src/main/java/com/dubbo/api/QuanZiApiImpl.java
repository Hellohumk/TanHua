package com.dubbo.api;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.api.service.PidService;
import com.dubbo.pojo.*;
import com.mongodb.client.result.DeleteResult;
import com.dubbo.pojo.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;


@Service(version = "1.0.0")
public class QuanZiApiImpl implements QuanZiApi {

    //pid自增
    @Autowired
    private PidService pidService;
    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public boolean savePublish(Publish publish) {

        //校验publish对象
        if(publish.getUserId() == null){
            return false;
        }

        try{
            publish.setCreated(System.currentTimeMillis());
            publish.setId(ObjectId.get());//ID就是当前时间点 new Date
            publish.setSeeType(1);//why?????????????

            //day08 增加一个自增长的pid
            publish.setPid(pidService.createId("publish",publish.getId().toString()));

            //publish save
            mongoTemplate.save(publish);//不需要指定表，你的pojo制定了就行

            //写入自己的相册表中
            Album album = new Album();
            album.setId(ObjectId.get());
            album.setPublishId(publish.getId());
            album.setCreated(System.currentTimeMillis());

            //album save
            mongoTemplate.save(album,"quanzi_album_" + publish.getUserId());


//            timeLine save
            Criteria criteria =
                    Criteria.where("userId").is(publish.getUserId());
            //找到自己的朋友们
            List<Users> users =
                    mongoTemplate.find(Query.query(criteria), Users.class);
            for(Users user : users) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setPublishId(publish.getId());
                timeLine.setUserId(user.getUserId());
                timeLine.setDate(System.currentTimeMillis());
                //依次保存到他的朋友们的时间线表
                mongoTemplate.save(timeLine, "quanzi_time_line_" +
                        user.getFriendId());
            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
            //TODO 你回滚要我写？不用Transactional？
        }

        //能到这也是因为error吧
        return false;
    }

    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize){
        Pageable pageable = PageRequest.of(page - 1,pageSize,Sort.by(Sort.Order.desc("created")));

        //这里将查询推荐动态和查询目标用户的朋友圈（时间线表）放在同一个api
        String collectionName = "quanzi_time_line_" + userId;
        if(userId == null){
            //如果未传入用户id，则表示查询推荐时间线表
            collectionName = "quanzi_time_line_recommend";//这个是推荐表？
        }


        //查询时间线表
        Query query = new Query().with(pageable);
        List<TimeLine> timeLineList = mongoTemplate.find(query,TimeLine.class,collectionName);

        //取出所有的PublishId，把所有具体的Publish查出
        List<ObjectId> publishIds = new ArrayList<>();
        for (TimeLine timeLine : timeLineList) {
            publishIds.add(timeLine.getPublishId());
        }

        //查询发布信息
        Query queryPublish =
                Query.query(Criteria.where("id").in(publishIds)).with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = this.mongoTemplate.find(queryPublish,
                Publish.class);//不用指定mongo表，因为就一个对应上了
        //组装pageInfo
        PageInfo<Publish> pageInfo = new PageInfo<Publish>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(publishList);
        pageInfo.setTotal(0);//why
        return pageInfo;
    }

    @Override
    public boolean saveLikeComment(Long userId, String publishId) {
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(1));//喜欢
        long count = mongoTemplate.count(query, Comment.class);//查这样的喜欢有无
        if(count > 0){
            //你不能重复喜欢
            return false;
        }
        //存入comment表
        return this.saveComment(userId,publishId,1,null);

    }

    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        Query query = Query.query(Criteria
                .where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(commentType));
        //DeleteResult 将删除后我们可能需要拿回的一些信息封装，有api供我们调用
        DeleteResult remove = this.mongoTemplate.remove(query,
                Comment.class);
        //删除是否成功
        return remove.getDeletedCount() > 0;
    }

    /**
     * 喜欢保存
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public boolean saveLoveComment(Long userId, String publishId) {
        Query query = Query.query(Criteria
                .where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(3));
        long count = mongoTemplate.count(query, Comment.class);//查这样的喜欢有无
        if(count > 0){
            //你不能重复喜欢
            return false;
        }
        //存入comment表
        return this.saveComment(userId,publishId,3,null);
    }

    /**
     * 评论（真） 保存
     * @param userId
     * @param publishId
     * @param type
     * @param content
     * @return
     */
    @Override
    public boolean saveComment(Long userId, String publishId, Integer type, String content) {
        try{
            //封装
            Comment comment = new Comment();
            comment.setId(ObjectId.get());
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setPublishId(new ObjectId(publishId));
            comment.setCommentType(type);
            comment.setCreated(System.currentTimeMillis());

            // 设置发布人的id
            Publish publish = this.mongoTemplate.findById(comment.getPublishId(), Publish.class);
            if (null != publish) {
                comment.setPublishUserId(publish.getUserId());
            }
            //没小视频，直接省

            this.mongoTemplate.save(comment);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Long queryCommentCount(String publishId, Integer type) {
        Query query =
                Query.query(Criteria.where("publishId").is(publishId).and("commentType").is(type));
        return this.mongoTemplate.count(query, Comment.class);
    }

    @Override
    public Publish queryPublishById(String id) {
        return mongoTemplate.findById(new ObjectId(id),Publish.class);
    }

    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page-1,pageSize,Sort.by(Sort.Order.desc("created"))); //jiangxu

        Query query = new Query(Criteria.where("publishId").is(new ObjectId((publishId))).and("commentType").is(2)).with(pageRequest);

        //查出所有评论
        List<Comment> timeLineList = mongoTemplate.find(query,Comment.class);

        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);
        pageInfo.setRecords(timeLineList);

        return pageInfo;

    }

    @Override
    public PageInfo<Comment> queryCommentListByUser(Long userId, Integer
            type, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                Sort.by(Sort.Order.desc("created")));
        Query query = new Query(Criteria
                .where("publishUserId").is(userId) //修改后相当于查询的是，我所发布帖子下所有的comment；若为userId则是我所评论的所有的comment
                .and("commentType").is(type)).with(pageRequest);
        List<Comment> commentList = this.mongoTemplate.find(query,
                Comment.class);
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(commentList);
        pageInfo.setTotal(0); //不提供总数
        return pageInfo;
    }

    /**
     * 用户的相册分页查询  相册表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public PageInfo<Publish> queryAlbumList(Long userId, Integer page,
                                            Integer pageSize) {
        PageInfo<Publish> pageInfo = new PageInfo<>();//result
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0); //不提供总数

        //查询
        //条件准备
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                Sort.by(Sort.Order.desc("created")));

        Query query = new Query().with(pageRequest);

        //查询  这里返回的是所有该用户发布的publish的id
        List<Album> albumList = this.mongoTemplate.find(query, Album.class,
                "quanzi_album_" + userId);

        if(CollectionUtils.isEmpty(albumList)){
            return pageInfo;
        }

        //拿到该用户发布的publish的id
        List<ObjectId> publishIds = new ArrayList<>();
        for (Album album : albumList) {
            publishIds.add(album.getPublishId());
        }
//查询发布信息
        Query queryPublish =
                Query.query(Criteria.where("id").in(publishIds)).with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = this.mongoTemplate.find(queryPublish,
                Publish.class);

        //封装
        pageInfo.setRecords(publishList);
        return pageInfo;
    }


}
