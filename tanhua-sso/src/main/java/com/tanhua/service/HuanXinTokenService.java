package com.tanhua.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.config.HuanxinConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
/*
组装http请求实现环信第三方提供的api
 */


@Service
public class HuanXinTokenService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private HuanxinConfig huanXinConfig;
    @Autowired
    private RestTemplate restTemplate;
    public static final String REDIS_KEY = "HX_TOKEN";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String refreshToken(){

        //组装与发送
        String targetUrl = this.huanXinConfig.getUrl() +
                this.huanXinConfig.getOrgName() + "/" + this.huanXinConfig.getAppName() +
                "/token";
        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credentials");
        param.put("client_id", this.huanXinConfig.getClientId());
        param.put("client_secret", this.huanXinConfig.getClientSecret());
//请求环信接口
        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(targetUrl, param,
                        String.class);
        if(responseEntity.getStatusCodeValue() != 200){
            return null;//g!
        }

        String body = responseEntity.getBody();
        try{
            JsonNode jsonNode = MAPPER.readTree(body);
            String accessToken = jsonNode.get("access_token").asText();
            if (StringUtils.isNotBlank(accessToken)) {
// 将token保存到redis，有效期为5天，环信接口返回的有效期为6天
                //exit
                this.redisTemplate.opsForValue().set(REDIS_KEY,
                        accessToken, Duration.ofDays(5));
                return accessToken;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String getToken() {
        String token = redisTemplate.opsForValue().get(REDIS_KEY);
        if(StringUtils.isBlank(token)){//token 为空白，即没有，就重新获取一下
            return refreshToken();
        }
        return token;
    }
}
