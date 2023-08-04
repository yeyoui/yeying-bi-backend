package com.yeyou.yeyingBIbackend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedisIdWorkerTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    public void testGenId(){
        String prefix = "test:lhy";
        System.out.println(redisIdWorker.nextInc(prefix));
        System.out.println(redisIdWorker.nextId(prefix));
    }
}
