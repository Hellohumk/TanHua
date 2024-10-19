package com.annotation;

import java.lang.annotation.*;

/**
 * 标记不用authorization'验证的controller方法
 */


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface NoAuthorization {
}
