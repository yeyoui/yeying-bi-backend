package com.yeyou.yeyingBIbackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yeyou.yeyingBIbackend.constant.RabbitmqConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

            //声明交换机
            String BI_EXCHANGE = BiMqConstant.BI_EXCHANGE;
            channel.exchangeDeclare(BI_EXCHANGE, RabbitmqConstant.DIRECT);
            //声明队列
            String BI_QUEUE = BiMqConstant.BI_QUEUE;
            channel.queueDeclare(BI_QUEUE, true, false, false, null);
            //绑定
            channel.queueBind(BI_QUEUE, BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY);
        } catch (IOException | TimeoutException e) {
            log.error("初始化交换机和队列失败，", e);
        }
    }
}
