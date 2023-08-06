package com.yeyou.yeyingBIbackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yeyou.yeyingBIbackend.constant.RabbitmqConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Rabbitmq交换机队列初始化
 */
@Slf4j
public class BiMqInit {

    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            connectionFactory.setPassword("109712");
            connectionFactory.setVirtualHost("yeyou_broker");
            connectionFactory.setUsername("yeyoui");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            //声明死信交换机
            channel.exchangeDeclare(BiMqConstant.BI_DEAD_EXCHANGE, RabbitmqConstant.DIRECT,true,false,null);
            //声明死信队列
            channel.queueDeclare(BiMqConstant.BI_DEAD_QUEUE, true, false, false, null);
            //死信队列绑定死信交换机
            channel.queueBind(BiMqConstant.BI_DEAD_QUEUE, BiMqConstant.BI_DEAD_EXCHANGE, BiMqConstant.BI_FAIL_ROUTING_KEY);

            //声明交换机
            String BI_EXCHANGE = BiMqConstant.BI_EXCHANGE;
            channel.exchangeDeclare(BI_EXCHANGE, RabbitmqConstant.DIRECT,true,false,null);
            // 创建用于指定死信队列的参数的Map对象
            Map<String, Object> deadArgs = new HashMap<>();
            deadArgs.put("x-dead-letter-exchange", BiMqConstant.BI_DEAD_EXCHANGE);
            deadArgs.put("x-dead-letter-routing-key", BiMqConstant.BI_FAIL_ROUTING_KEY);
            //声明队列
            String BI_QUEUE = BiMqConstant.BI_QUEUE;
            channel.queueDeclare(BI_QUEUE, true, false, false, deadArgs);
            //绑定
            channel.queueBind(BI_QUEUE, BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY);

            //声明死信交换机
            channel.exchangeDeclare(BiMqConstant.ORDER_DEAD_EXCHANGE, RabbitmqConstant.DIRECT,true,false,null);
            //声明死信队列
            channel.queueDeclare(BiMqConstant.ORDER_DEAD_QUEUE, true, false, false, null);
            //死信队列绑定死信交换机
            channel.queueBind(BiMqConstant.ORDER_DEAD_QUEUE, BiMqConstant.ORDER_DEAD_EXCHANGE, BiMqConstant.ORDER_FAIL_ROUTING_KEY);

            //声明交换机
            channel.exchangeDeclare(BiMqConstant.ORDER_DELAY_EXCHANGE, RabbitmqConstant.DIRECT,true,false,null);
            // 创建用于指定死信队列的参数的Map对象
            //设置Order延迟队列TTL
            Map<String, Object> orderDelayArgs = new HashMap<>();
            //TTL60秒
            orderDelayArgs.put("x-message-ttl", 30000);
            //设置死信交换机和key
            orderDelayArgs.put("x-dead-letter-exchange", BiMqConstant.ORDER_DEAD_EXCHANGE);
            orderDelayArgs.put("x-dead-letter-routing-key", BiMqConstant.ORDER_FAIL_ROUTING_KEY);
            //声明队列
            channel.queueDeclare(BiMqConstant.ORDER_DELAY_QUEUE, true, false, false, orderDelayArgs);
            //绑定
            channel.queueBind(BiMqConstant.ORDER_DELAY_QUEUE, BiMqConstant.ORDER_DELAY_EXCHANGE, BiMqConstant.ORDER_DELAY_ROUTING_KEY);
        } catch (IOException | TimeoutException e) {
            log.error("初始化交换机和队列失败，", e);
        }
    }

}
