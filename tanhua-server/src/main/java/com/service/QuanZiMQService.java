package com.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.api.QuanZiApi;
import com.pojo.Publish;
import com.pojo.User;
import com.utils.UserThreadLocal;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QuanZiMQService {
    private static final Logger LOGGER  = LoggerFactory.getLogger(QuanZiMQService.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    //一系列影响分数的逻辑
    /**
     * 发布动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean publishMsg(String publishId) {
        return this.sendMsg(publishId, 1);
    }
    /**
     * 浏览动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean queryPublishMsg(String publishId) {
        return this.sendMsg(publishId, 2);
    }

    /**
     * 点赞
     * @param publishId
     * @return
     */
    public Boolean likePublishMsg(String publishId) {
        return this.sendMsg(publishId, 3);
    }

    /**
     * 取消点赞动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean disLikePublishMsg(String publishId) {
        return this.sendMsg(publishId, 6);
    }
    /**
     * 喜欢动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean lovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 4);
    }
    /**
     * 取消喜欢动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean disLovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 7);
    }
    /**
     * 评论动态消息
     *
     * @param publishId
     * @return
     */
    public Boolean commentPublishMsg(String publishId) {
        return this.sendMsg(publishId, 5);
    }
    /**
     * 发送圈子操作相关的消息
     *
     * @param publishId
     * @param type 1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点
    赞，7-取消喜欢
     * @return
     */
    public Boolean sendMsg(String publishId,Integer type){
        User user = UserThreadLocal.get();

        if(user == null) {
            return false;
        }

        try{
            Publish publish = this.quanZiApi.queryPublishById(publishId);
    //构建消息
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", user.getId());//操作的人
            msg.put("date", System.currentTimeMillis());
            msg.put("publishId", publishId);//publishId
            msg.put("pid", publish.getPid());
            msg.put("type", type);

            //MQ也可以传一个Map
            //这里mq把其主要信息构建好后传给推荐系统，分数逻辑在spark引擎上实现
            this.rocketMQTemplate.convertAndSend("tanhua-quanzi", msg);
        } catch (Exception e) {
            LOGGER.error("发送消息失败! publishId = " + publishId + ", type = " + type, e);
            return false;
        }
        return true;

    }

    public void sendCommentPublishMsg(String publishId) {
        sendMsg(publishId,5);
    }
}
