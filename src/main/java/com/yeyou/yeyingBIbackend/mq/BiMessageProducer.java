package com.yeyou.yeyingBIbackend.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息生产者
 */
@Component
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 生产者发送消息
     * @param message 消息
     */
    public void sendMsg(String message){
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY, message);
    }
}
