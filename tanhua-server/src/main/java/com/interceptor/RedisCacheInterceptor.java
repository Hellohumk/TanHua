package com.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*
相当于统一的去做缓存的处理，而不是再每个接口中都去实现（例如之前的 user 和 token，都是在对应接口中单独实现的）
 */

/**
 * Servlet 实现
 */

@Component
public class RedisCacheInterceptor implements HandlerInterceptor {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${tanhua.cache.enable}")
    private Boolean enable;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,Object handler) throws Exception {

        if(! enable){
            //未开启缓存
            return true;
        }

        String method = request.getMethod();
        if(!StringUtils.equalsAnyIgnoreCase(method,"GET","POST")) {
            //非GET POST不缓存（只有这两个有可能需要缓存结果）
            return true;
        }

        //缓存做命中，查询redis
        //redisKey策略： md5(请求的url + 请求参数)
        String redisKey = createRedisKey(request);
        String data = redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isEmpty(data)) {
            //no hit !
            return true;
        }

        //data数据进行响应
        //hit 就不放进去，因为你想要的结果就在redis中
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(data);

        return false;
    }

    /**
     * redisKey 策略
     *
     *
     * 策略： SERVER_DATA_ + MD5( url + paramMap + _ + authorization(token) )
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static String createRedisKey(HttpServletRequest request) throws Exception{
        String paramStr = request.getRequestURI();//url
        Map<String,String[]> parameterMap = request.getParameterMap();//参数表
        if(parameterMap.isEmpty()) {
            //代码会读取请求的输入流，并将其转换为字符串。
            //这里使用了getInputStream（） 取出后输入流就没有了，这是一次性资源 TODO 加Interceptor
            paramStr += IOUtils.toString(request.getInputStream(), "UTF-8");
        } else {
            paramStr += MAPPER.writeValueAsString(request.getParameterMap()); //参数写入
        }


        String authorization = request.getHeader("Authorization");
        if(StringUtils.isNotEmpty(authorization)){
            paramStr += "_" + authorization;
        }

        return "SERVER_DATA_" + DigestUtils.md5Hex(paramStr); //codec


    }

}
