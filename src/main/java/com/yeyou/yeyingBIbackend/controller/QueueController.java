package com.yeyou.yeyingBIbackend.controller;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.ReturnCallback;
import com.yeyou.yeyingBIbackend.mq.BiMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/queue")
@Profile({"dev", "local"})
@Slf4j
//用于测试消息队列使用，只在开发和本地环境可用
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/add")
    public void addTaskToThreadPool(String name) {
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中：" + name + "，执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String getThreadPoolState() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务数", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数", activeCount);
        return JSONUtil.toJsonStr(map);
    }

    @GetMapping("/sendOrderMsg")
    public String sendMessageToOrderExchange(String msg){
        biMessageProducer.sendMsgToDelayOrder(msg);
        return "发送成功";
    }

    private static final String TEST_CONFIRM_EXCHANGE = "testConfirm_exchange";
    private static final String TEST_CONFIRM_KEY = "testConfirm_key";
    @PostConstruct
    public void init(){
//        rabbitTemplate.setConfirmCallback(myCallBack);
    }
    @GetMapping("/sendConfirmMsg/{message}")
    public void sendConfirmMsg(@PathVariable("message") String msg){
        CorrelationData correlationData = new CorrelationData("1");
        rabbitTemplate.convertAndSend(TEST_CONFIRM_EXCHANGE,TEST_CONFIRM_KEY,msg,correlationData);
        correlationData = new CorrelationData("2");
        rabbitTemplate.convertAndSend(TEST_CONFIRM_EXCHANGE,"TEST_CONFIR111M_KEY",msg,correlationData);

    }
}
