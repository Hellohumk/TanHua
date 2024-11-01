package com.tanhua;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
@MapperScan("com.tanhua.mapper")
public class tanhuassoApplication {
    public static void main(String[] args) {
        SpringApplication.run(tanhuassoApplication.class, args);
    }
}