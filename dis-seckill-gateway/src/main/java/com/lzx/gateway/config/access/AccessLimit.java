package com.lzx.gateway.config.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {

    // 最大请求次数的时间间隔
    int seconds();

    // 最大请求次数
    int maxAccessCount();

    // 是否需要登录
    boolean needLogin() default true;
}
