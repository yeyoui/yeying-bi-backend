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
    public void sendMsgToBI(String message){
        sendMeg(BiMqConstant.BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY, message);
    }

    public void sendMsgToDelayOrder(String message){
        sendMeg(BiMqConstant.ORDER_DELAY_EXCHANGE, BiMqConstant.ORDER_DELAY_ROUTING_KEY, message);
    }

    public void sendMeg(String exchangeName,String routingKey,String message){
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
