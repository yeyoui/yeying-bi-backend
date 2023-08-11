package com.yeyou.yeyingBIbackend.manager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 系统内部服务限流工具
 */
@Service
@Slf4j
public class RedissonRateLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 通过Redisson实现限流
     * @param sign 限流的标识 一般为ID
     */
    public void doRateLimiter(String sign,long rate,long rateInterval){
        String key = "limit:user:";
        //根据用户ID限流
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key+sign);
        //一秒一次
        rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, RateIntervalUnit.SECONDS);
        //尝试获取令牌
//        boolean result = rateLimiter.tryAcquire(1);
        while (!rateLimiter.tryAcquire(1)){
            log.warn("[{}]，请求速率超过限制",sign);
            try {
                Thread.sleep(1000*rateInterval/rate);
            } catch (InterruptedException e) {
                log.error("等待限流时被打断，",e);
            }
        }
    }

}
