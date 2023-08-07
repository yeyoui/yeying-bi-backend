package com.yeyou.yeyingBIbackend.manager;

import jodd.time.TimeUtil;
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

    public boolean incAndLimitCount(String keyName, int cnt, long time, TimeUnit timeUtil){
        Integer size = (Integer) redisTemplate.opsForValue().get(keyName);
        //键不存在
        if (size==null){
            redisTemplate.opsForValue().set(keyName, 1,time,timeUtil);
            return true;
        }
        //判断并且增长
        if(size>=cnt){
            //超过限制
            return false;
        }
        redisTemplate.opsForValue().increment(keyName, 1);
        return true;
    }
}
