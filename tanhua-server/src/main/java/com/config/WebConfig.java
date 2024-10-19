package com.config;

import com.interceptor.RedisCacheInterceptor;
import com.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RedisCacheInterceptor redisCacheInterceptor;

    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * redis拦截器注册
     * @param registry
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(this.tokenInterceptor).addPathPatterns("/**");


        registry.addInterceptor(this.redisCacheInterceptor).addPathPatterns("/**");
    }


}
