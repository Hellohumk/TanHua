package com.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dubbo.api.RecommendUserApi;
import com.dubbo.pojo.RecommendUser;
import com.dubbo.pojo.vo.PageInfo;
import com.pojo.vo.TodayBest;
import org.springframework.stereotype.Service;

@Service
public class RecommendUserService {

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    public TodayBest queryTodayBest(Long id){
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(id);
        if(recommendUser != null){
            //填充todaybest 的 分数
            TodayBest todayBest = new TodayBest();
            todayBest.setId(id);
            //score
            todayBest.setFateValue(Double.valueOf(Math.floor(recommendUser.getScore())).longValue()); //取zhengshu
            return todayBest;

        }

        return null;
    }

    /**
     * 查询佳人列表 （所有spark加入到表中的）
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    public PageInfo<RecommendUser> queryRecommendUserList(Long id,Integer page,Integer pageSize){
        return recommendUserApi.queryPageInfo(id,page,pageSize);
    }

    /**
     * 查询某个佳人的score
     * @param userId
     * @param toUserId
     * @return
     */
    double queryScore(Long userId, Long toUserId) {
        return this.recommendUserApi.queryScore(userId, toUserId);
    }


}
