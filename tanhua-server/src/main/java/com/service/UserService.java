package com.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 这里相当于是调别人的controller，为什么不把对面的service也注册成provider调用service
 * 即为什么不把sso拆成一个dubbo
 */


@Service
public class UserService {
    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Value("${tanhua.sso.url}")
    private String url;


    /**
     * 只要你登陆过，redis必有你的信息，才会到这。没登陆直接拦截了
     * @param token
     * @return
     */
    public User queryUserByToken(String token) {
        //调get请求拿token ps.这个模块没redis
        String data = this.restTemplate.getForObject(url + "/user/{token}",
                String.class, token);
        if (StringUtils.isNotEmpty(data)) {
            try {
                return MAPPER.readValue(data, User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
