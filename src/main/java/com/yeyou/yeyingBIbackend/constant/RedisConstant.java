package com.yeyou.yeyingBIbackend.constant;

/**
 * Redis的通用常量
 */
public interface RedisConstant {
    String BI_NOTIFY_UID="bi:notify:uid:";
    String BI_RETRY_KEY = "bi:retry:key:";
    long CACHE_NULL_TTL = 5;

    //限流缓存配置
    String BI_RATE_LIMIT_RATE_KEY ="bi:rateLimit:";
    String BI_RATE_LIMIT_RATE_LOCK="bi:rateLimit:lock";
    //1分钟过期
    long BI_RATE_LIMIT_RATE_TTL=1;

}
