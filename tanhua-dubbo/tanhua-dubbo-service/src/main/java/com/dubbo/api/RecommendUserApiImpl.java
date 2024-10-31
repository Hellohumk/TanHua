package com.dubbo.api;

import com.dubbo.api.RecommendUserApi;
import com.dubbo.pojo.RecommendUser;
import com.dubbo.pojo.vo.PageInfo;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")   //这个是DUBBO 的service，意味着作为服务模块，要被远程调用
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 找到分最高的人
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {

        //找到一条 推荐人为xxx的，分数倒叙排列的第一条（即分数最大的）
        Query query = Query.query(Criteria.where("toUserId").is(userId))
                .with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return this.mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 从分高到分低，分页查出所有被推荐的人
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */

    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {

        //第一页为第0页
        PageRequest pageRequest = PageRequest.of(pageNum - 1,pageSize,Sort.by(Sort.Order.desc("score")));

        //with是一个多态，可以接受很多类型的东西
        Query query = Query.query(Criteria.where("toUserId").is(userId)).with(pageRequest);


        List<RecommendUser> recommendUserList = mongoTemplate.find(query, RecommendUser.class);


        return new PageInfo<>(0, pageNum, pageSize, recommendUserList);
    }

    /**
     * 找到某个佳人的socre
     * @param userId
     * @param toUserId
     * @return
     */
    @Override
    public double queryScore(Long userId, Long toUserId) {
        Query query = Query.query(Criteria
                .where("toUserId").is(toUserId)
                .and("userId").is(userId));
        RecommendUser recommendUser = this.mongoTemplate.findOne(query,
                RecommendUser.class);
        if (null == recommendUser) {
            return 0;
        }
        return recommendUser.getScore();
    }
}
