package com.yeyou.yeyingBIbackend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RateLimiterTest {
    @Resource
    private RedissonRateLimiterManager rateLimiterManager;
    private static final String SIGN="LHY";

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
}
