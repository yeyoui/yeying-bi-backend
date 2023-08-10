package com.yeyou.yeyingBIbackend.model.bo;

import lombok.Data;

/**
 * 用于存储限流配置
 */
@Data
public class RedisRateLimitConfig {
    /**
     * 限流的时间(单位为:秒)，比如1分钟内最多1000个请求。注意我们这个限流器不是很精确，但误差不会太大
     */
    long rateInterval;

    /**
     * 限流的次数，比如1分钟内最多1000个请求。注意count的值不能小于1,必须大于等于1
     */
    long rate;

    /**
     * Redis中的键名
     */
    String redisKey;
}
