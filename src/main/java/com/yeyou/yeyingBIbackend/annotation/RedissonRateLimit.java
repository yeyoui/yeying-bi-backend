package com.yeyou.yeyingBIbackend.annotation;

import java.lang.annotation.*;

/**
 * Redisson限流
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented // 生成javadoc时包含该注解
@Inherited // 此注解允许被集成
public @interface RedissonRateLimit {
    /**
     * 限流的时间(单位为:秒)，比如1分钟内最多1000个请求。注意我们这个限流器不是很精确，但误差不会太大
     */
    long rateInterval() default 1;

    /**
     * 限流的次数，比如1分钟内最多1000个请求。注意count的值不能小于1,必须大于等于1
     */
    long rate() default 1;

    /**
     * 限流预设 1.bi请求服务预设
     */
    int limitPreset();
}
