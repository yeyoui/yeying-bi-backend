package com.yeyou.yeyingBIbackend.mq;

public interface BiMqConstant {
    String BI_EXCHANGE = "bi_exchange";
    String BI_DEAD_EXCHANGE = "bi_dlx-direct_exchange";
    String BI_QUEUE = "bi_queue";
    String BI_DEAD_QUEUE = "bi_dlx_queue";
    String BI_ROUTING_KEY = "bi_routingKey";
    String BI_FAIL_ROUTING_KEY = "bi_fail_routingKey";

    String ORDER_DELAY_EXCHANGE = "order_exchange";
    String ORDER_DEAD_EXCHANGE = "order_dlx-direct_exchange";
    String ORDER_DELAY_QUEUE = "order_delay_queue";
    String ORDER_DEAD_QUEUE = "order_dlx_queue";
    String ORDER_DELAY_ROUTING_KEY = "order_delay_routingKey";
    String ORDER_FAIL_ROUTING_KEY = "order_fail_routingKey";
}
