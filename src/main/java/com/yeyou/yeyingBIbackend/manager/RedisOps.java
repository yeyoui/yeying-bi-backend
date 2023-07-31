package com.yeyou.yeyingBIbackend.manager;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisOps {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 信息入队
     * @param queueName 队列名
     * @param value 值
     */
    public void enqueue(String queueName,String value){
        redisTemplate.opsForList().rightPush(queueName,value);
    }

    /**
     * 获取队列信息
     * @param queueName 队列名
     * @return
     */
    public String dequeueBlock(String queueName){
        Object value = redisTemplate.opsForList().leftPop(queueName);
        return String.valueOf(value);
    }
}
