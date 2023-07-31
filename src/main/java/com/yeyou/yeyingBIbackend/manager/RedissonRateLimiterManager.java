package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
@Slf4j
public class RedissonRateLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 通过Redisson实现限流
     * @param sign 限流的标识
     */
    public void doRateLimiter(String sign){
        //根据用户ID限流
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(sign);
        //一秒一次
        rateLimiter.trySetRate(RateType.OVERALL, 1, 3, RateIntervalUnit.SECONDS);
        //尝试获取令牌
        boolean result = rateLimiter.tryAcquire(1);
        if(!result){
            log.warn("用户id：{}，请求速率超过限制",sign);
            throw new BusinessException(ErrorCode.RATE_LIMITER_ERROR);

        }
    }

}
