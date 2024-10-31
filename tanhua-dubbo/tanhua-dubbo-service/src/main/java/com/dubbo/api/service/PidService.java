package com.dubbo.api.service;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/*
使用redis实现自增长id
 */
@Service
public class PidService {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 生成自增长pid
     * @return
     */
    public Long createId(String type,String objectId) {

        //转大写
        type = StringUtils.upperCase(objectId);

        //如果该publish已经被存储过pid了，不允许再创建（有什么情况会再创建pid？）
        // (hashkey , objectId) : pid
        String hashKey = "TANHUA_HASH_ID_" + type;
        //如果ObjectId已经存在的话，就返回对应的id
        if(redisTemplate.opsForHash().hasKey(hashKey,objectId)){
            return Long.valueOf(redisTemplate.opsForHash().get(hashKey,objectId).toString());
        }

        String key = "TANHUA_ID_" + type;
        Long increment = redisTemplate.opsForValue().increment(key);

        //生成的pid和id进行绑定
        redisTemplate.opsForHash().put(hashKey,objectId,increment);

        return increment;


    }
}
