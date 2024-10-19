package com.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interceptor.RedisCacheInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Duration;

/**
 * 很明显这是通过Spring实现的，所以与HttpServletRequest无关
 */


@ControllerAdvice  //指明并加入Bean
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 只对谁进行操作
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class
            converterType) {
//支持的处理类型，这里仅对get和post进行处理
        return returnType.hasMethodAnnotation(GetMapping.class) ||
                returnType.hasMethodAnnotation(PostMapping.class);
    }

    /**
     * 操作逻辑！
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        try {
            String redisKey =
                    RedisCacheInterceptor.createRedisKey(((ServletServerHttpRequest) request).getServletRequest());
            String redisValue;
            if (body instanceof String) {
                redisValue = (String) body;
            } else {
                redisValue = MAPPER.writeValueAsString(body);
            }
//存储到redis中
            this.redisTemplate.opsForValue().set(redisKey, redisValue,
                    Duration.ofHours(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

}
