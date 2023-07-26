package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
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
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);
        //尝试获取令牌
        boolean result = rateLimiter.tryAcquire(1);
        if(!result){
//            throw new BusinessException(ErrorCode.RATE_LIMITER_ERROR);
            System.out.println("限流！！！");
        }
    }

}
