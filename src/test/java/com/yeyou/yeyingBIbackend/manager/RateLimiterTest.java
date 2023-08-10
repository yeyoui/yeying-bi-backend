package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.constant.LimitPresetConstant;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.model.bo.RedisRateLimitConfig;
import com.yeyou.yeyingBIbackend.service.RateLimitInfoService;
import com.yeyou.yeyingBIbackend.utils.StringRedisCacheUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RateLimiterTest {
    @Resource
    private RedissonRateLimiterManager rateLimiterManager;
    private static final String SIGN="LHY";
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;
    @Resource
    private RateLimitInfoService rateLimitInfoService;

    @Test
    public void tryMoreRequest() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            rateLimiterManager.doRateLimiter(SIGN);
            System.out.println(i);
        }
        Thread.sleep(1000);
        for (int i = 0; i < 5; i++) {
            rateLimiterManager.doRateLimiter(SIGN);
            System.out.println(i);
        }
    }

    @Test
    public void changeRateLimitStrategy(){
        String redisKey = rateLimitInfoService.query().eq("limitPreset", LimitPresetConstant.BI_GEN_CHART).one().getRedisKey();
        //网络拥挤
        RedisRateLimitConfig redisRateLimitConfig = new RedisRateLimitConfig();
        redisRateLimitConfig.setRateInterval(1);
        redisRateLimitConfig.setRate(1);
        redisRateLimitConfig.setRedisKey(redisKey);
        stringRedisCacheUtils.set(RedisConstant.BI_RATE_LIMIT_RATE_KEY+1,redisRateLimitConfig,2, TimeUnit.MINUTES);
    }
}
