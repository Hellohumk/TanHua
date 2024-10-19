package com.tanhua.recommend.consumer;


import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.recommend.pojo.Publish;
import com.tanhua.recommend.pojo.RecommendQuanZi;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/*
推荐圈子 消费者 逻辑实现

mongo 推荐圈子insert口
 */

@Component

//指定监听器
@RocketMQMessageListener(topic = "tanhua-quanzi",
        consumerGroup = "tanhua-quanzi-consumer")
public class QuanZiMsgConsumer implements RocketMQListener<String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(QuanZiMsgConsumer.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    //消息进行过滤
    @Override
    public void onMessage(String msg) {
        try{
            JsonNode jsonNode = MAPPER.readTree(msg); //string 转json
            //通过json拿出消息
            Long userId = jsonNode.get("userId").asLong();
            Long pid = jsonNode.get("pid").asLong();
            String publishId = jsonNode.get("publishId").asText();
            Integer type = jsonNode.get("type").asInt();

            //1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜huan

            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            recommendQuanZi.setUserId(userId);
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setDate(System.currentTimeMillis());
            recommendQuanZi.setPublishId(pid);

            //加分逻辑
            switch( type ){
                case 1: {
                    int score = 0;
                    Publish publish = mongoTemplate.findById(new ObjectId(publishId),Publish.class);

                    //字数
                    if (StringUtils.length(publish.getText()) < 50) {
                        score += 1;
                    } else if (StringUtils.length(publish.getText()) <
                            100) {
                        score += 2;
                    } else if (StringUtils.length(publish.getText()) >=
                            100) {
                        score += 3;
                    }
                    if (!CollectionUtils.isEmpty(publish.getMedias())) {
                        score += publish.getMedias().size();//几张图加几分
                    }
                    recommendQuanZi.setScore(Double.valueOf(score));
                    break;
                }
                case 2: {
                    recommendQuanZi.setScore(1d);
                    break;
                }
                case 3: {
                    recommendQuanZi.setScore(5d);
                    break;
                }
                case 4: {
                    recommendQuanZi.setScore(8d);
                    break;
                }
                case 5: {
                    recommendQuanZi.setScore(10d);
                    break;
                }
                case 6: {
                    recommendQuanZi.setScore(-5d);
                    break;
                }
                case 7: {
                    recommendQuanZi.setScore(-8d);
                    break;
                }
                default: {
                    recommendQuanZi.setScore(0d);
                    break;
                }
            }

            //这里collection是mongo  上面collection是List的爹
            String collectionName = "recommend_quanzi_" + new DateTime().toString();
            mongoTemplate.save(recommendQuanZi,collectionName);




        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error("failed to consume" + msg , e);
        }
    }
}
