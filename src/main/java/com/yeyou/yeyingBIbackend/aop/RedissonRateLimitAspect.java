package com.yeyou.yeyingBIbackend.aop;

import com.google.common.reflect.TypeToken;
import com.yeyou.yeyingBIbackend.annotation.RedissonRateLimit;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.model.bo.RedisRateLimitConfig;
import com.yeyou.yeyingBIbackend.model.entity.RateLimitInfo;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.service.RateLimitInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.NetUtils;
import com.yeyou.yeyingBIbackend.utils.StringRedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Aspect
public class RedissonRateLimitAspect {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserService userService;
    @Resource
    private RateLimitInfoService rateLimitInfoService;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;

    //检查Redisson是否在容器中
    @PostConstruct
    public void init(){
        if(redissonClient==null){
            log.error("Spring容器中没有RedissonClient，Redisson限流器将无法使用...");
        }
        //设置预设默认参数
    }

    /**
     * 限流切面逻辑（前置通知）
     * 在所有带有@RedissonRateLimit注解方法执行前，先进入切面逻辑
     * @param redissonRateLimit 注解信息
     */
    @Before("@annotation(redissonRateLimit)")
    public void redissonRateLimitCheck(RedissonRateLimit redissonRateLimit){
        //用户信息
        HttpServletRequest httpServletRequest = NetUtils.getHttpServletRequest();
        User loginUser = userService.getLoginUser(httpServletRequest);
        //获取限流器
        RRateLimiter rateLimiter = getRateLimiter(redissonRateLimit,loginUser.getId());
        //尝试获取令牌
        boolean result = rateLimiter.tryAcquire(1);
        if(!result){
            log.warn("用户id：{}，请求速率超过限制",loginUser.getId());
            throw new BusinessException(ErrorCode.RATE_LIMITER_ERROR);
        }
    }

    /**
     * 获取限流管理对象
     * @param redissonRateLimit 限流注解信息
     * @param uid 用户ID
     * @return 限流管理对象
     */
    @NotNull
    private RRateLimiter getRateLimiter(RedissonRateLimit redissonRateLimit,long uid) {
        String key = String.valueOf(uid);
        long rate = redissonRateLimit.rate();
        long rateInterval = redissonRateLimit.rateInterval();
        int ratePreset = redissonRateLimit.limitPreset();

        //获取限流配置信息
        Type type = new TypeToken<RedisRateLimitConfig>(){}.getType();
        RedisRateLimitConfig redisRateLimitConfig = stringRedisCacheUtils.queryWithLock(
                RedisConstant.BI_RATE_LIMIT_RATE_KEY,
                ratePreset,
                type,
                RedisConstant.BI_RATE_LIMIT_RATE_LOCK + ratePreset,
                this::getRedisRateLimitConfig, RedisConstant.BI_RATE_LIMIT_RATE_TTL, TimeUnit.MINUTES);

        if(redisRateLimitConfig!=null){
            rateInterval = redisRateLimitConfig.getRateInterval();
            rate = redisRateLimitConfig.getRate();
            key = redisRateLimitConfig.getRedisKey() + key;
        }

        //根据用户ID限流
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //获取之前的配置信息
        RateLimiterConfig rateLimiterConfig = rateLimiter.getConfig();
        Long oldRateInterval = rateLimiterConfig.getRateInterval();
        Long oldRate = rateLimiterConfig.getRate();
        //判断限流策略是否更新
        if(oldRateInterval != TimeUnit.MILLISECONDS.convert(rateInterval,TimeUnit.SECONDS) &&
                oldRate != rate){
            //重新设置限流配置
            rateLimiter.delete();
            rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, RateIntervalUnit.SECONDS);
            //12小时自动过期
            rateLimiter.expire(Duration.ofHours(12));
        }
        return rateLimiter;
    }

    /**
     * 从数据库中获取限流配置信息
     * @param limitPreset 限流预设值
     * @return 限流配置信息
     */
    private RedisRateLimitConfig getRedisRateLimitConfig(int limitPreset){
        RateLimitInfo limitInfo = rateLimitInfoService.query().eq("limitPreset", limitPreset).one();
        RedisRateLimitConfig redisRateLimitConfig = new RedisRateLimitConfig();
        redisRateLimitConfig.setRateInterval(limitInfo.getRateInterval());
        redisRateLimitConfig.setRate(limitInfo.getRate());
        redisRateLimitConfig.setRedisKey(limitInfo.getRedisKey());
        return redisRateLimitConfig;
    }
}
