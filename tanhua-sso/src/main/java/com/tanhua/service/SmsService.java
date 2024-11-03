package com.tanhua.service;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String url = "https://open.ucpaas.com/ol/sms/sendsms";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 短信验证码
     */
    public String sendSms(String mobile){
        Map<String,Object> params = new HashMap<>();
        //TODO 记得注册好填写对应的东西
        params.put("sid","");
        params.put("token","");
        params.put("appid","");
        params.put("template","");
        params.put("mobile",mobile);

// 生成4位数验证
        params.put("param", RandomUtils.nextInt(100000, 999999));
        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(url, params, String.class); //发送你妈的post请求，带上自动生成的验证码

        String body = responseEntity.getBody();

        try{
            JsonNode jsonNode = MAPPER.readTree(body);
            //000000 发送成功
            if(StringUtils.equals(jsonNode.get("code").textValue(),"000000")){
                return String.valueOf(params.get("param"));
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 发送验证码 返回success or error
     * @param phone
     * @return
     */
    public Map<String, Object> sendCheckCode(String phone) {
        Map<String,Object> result = new HashMap<>(2);
        try {
            String rediskey = "CHECK_CODE_" + phone;
            String value = this.redisTemplate.opsForValue().get(rediskey);
            if(! StringUtils.isEmpty(value)){
                //不是空证明code还在，没失效,直接给用户再传过去，不用重新发sms
                result.put("code",1);//验证成功
                result.put("msg","上一次发送的验证码还没失效");
                return result;
            }
            //没验证码,说明已经过期或者就根本没验证
            //发消息
            String code = this.sendSms(phone);
            if(code == null){//code 是null就直接有问题
                throw new Exception("error");
            }

            //success
            result.put("code",3);
            result.put("msg","ok");

            //redis
            this.redisTemplate.opsForValue().set(rediskey,code, Duration.ofMinutes(2));//最后一个参数是有效时间

            return result;


        }catch(Exception e){
            //其他问题入口
            //是不是2好像没关系，因为他就没2的if
            result.put("code",2);
            result.put("msg","code g了");
            return result;
        }
    }
}
