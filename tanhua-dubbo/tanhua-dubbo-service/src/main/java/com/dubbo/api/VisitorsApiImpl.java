package com.dubbo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.api.VisitorsApi;
import com.dubbo.pojo.RecommendUser;
import com.dubbo.pojo.Visitors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String saveVisitor(Visitors visitors) {
        visitors.setId(ObjectId.get());
        visitors.setDate(System.currentTimeMillis());

        mongoTemplate.save(visitors);

        //返回他的id
        return visitors.getId().toHexString();


    }

    /**
     * 传进来的num是查几个，因为他这里写死了就查一页，傻逼
     * @param userId
     * @param num
     * @return
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Integer num) {
        Pageable pageable = PageRequest.of(0,num,Sort.by(Sort.Order.desc("date")));

        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageable);

        return this.queryVistorsList(query);
    }

    /**
     * 日期查寻
     * @param userId
     * @param date
     * @return
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Long date) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("date").is(date));

        return this.queryVistorsList(query);
    }

    //查 + 封装
    public List<Visitors> queryVistorsList(Query query) {
        List<Visitors> visitorsList = this.mongoTemplate.find(query,Visitors.class);

        //score 看过我的人的评分 TODO 不是你visitor表里不是有这一项吗，又不是VO，说明拿出来有score啊，为什么又要查？
        for(Visitors visitor : visitorsList){
            Query queryRecommend = Query.query(Criteria.where("toUserId").is(visitor.getUserId())//这里其实就是当前用户id，也是参数给进来的userId
                    .and("userId").is(visitor.getVisitorUserId()));

            RecommendUser recommendUser = mongoTemplate.findOne(queryRecommend,RecommendUser.class);//查到对应推荐人分数
            if(recommendUser != null){
                visitor.setScore(recommendUser.getScore());
            }else {
                //默认分 30
                visitor.setScore(30d);
            }
        }

        return visitorsList;

    }
}
