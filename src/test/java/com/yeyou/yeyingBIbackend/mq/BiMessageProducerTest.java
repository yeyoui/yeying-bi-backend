package com.yeyou.yeyingBIbackend.mq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class BiMessageProducerTest {
    @Resource
    private BiMessageProducer biMessageProducer;
    @Test
    void sendMsg() {
        biMessageProducer.sendMsg("你好");
    }
}
