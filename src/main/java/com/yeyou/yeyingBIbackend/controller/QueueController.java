package com.yeyou.yeyingBIbackend.controller;

import cn.hutool.json.JSONUtil;
import com.yeyou.yeyingBIbackend.mq.BiMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/queue")
@Profile({"dev", "local"})
@Slf4j
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;

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
}
