package com.yeyou.yeyingBIbackend.mq;

import com.github.rholder.retry.*;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.manager.RedisOps;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.InterfaceInfo;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.service.InterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.UserInterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 消息生产者
 */
@Component
@Slf4j
public class BiMessageProducer implements RabbitTemplate.ReturnsCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisOps redisOps;
    @Resource
    private ChartInfoService chartInfoService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Resource
    private UserService userService;
    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Value("${yeying.BI_INTERFACE_ID}")
    private long BI_INTERFACE_ID;

    @PostConstruct
    public void init() {
        //设置失败回调
        rabbitTemplate.setMandatory(true);
        //设置回调方法
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 生产者发送消息
     *
     * @param message 消息
     */
    public void sendMsgToBI(String message) {
        sendMeg(BiMqConstant.BI_EXCHANGE, BiMqConstant.BI_ROUTING_KEY, message);
    }

    public void sendMsgToDelayOrder(String message) {
        sendMeg(BiMqConstant.ORDER_DELAY_EXCHANGE, BiMqConstant.ORDER_DELAY_ROUTING_KEY, message);
    }

    public void sendMeg(String exchangeName, String routingKey, String message) {
        log.info("______");
        log.debug("准备发消息");
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        } catch (AmqpException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        String message = new String(returned.getMessage().getBody());
        log.error("消息:{}被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}",
                message, returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
        //发送BI请求异常处理
        if (BiMqConstant.BI_EXCHANGE.equals(returned.getExchange())) {
//                BiMqConstant.BI_ROUTING_KEY.equals(returned.getRoutingKey())) {
            //重试(最多重试3次)
            if (redisOps.incAndLimitCount(RedisConstant.BI_RETRY_KEY + message, 3, 5, TimeUnit.MINUTES)) {
                sendMeg(returned.getExchange(), returned.getRoutingKey(), message);
            } else {
                //设置任务失败
                boolean succeed = chartInfoService.update().eq("id", message).set("status", 3).update();
                if (!succeed) {
                    log.error("重试时设置任务失败出现异常，表格id[{}]", message);
                    return;
                }
                //补偿用户调用数
                ChartInfo chartInfo = chartInfoService.getById(message);
                if (chartInfo == null) {
                    log.error("获取图表ID出现异常，表格id[{}]", message);
                    return;
                }
                InterfaceInfo interfaceInfo = interfaceInfoService.getById(BI_INTERFACE_ID);
                //补偿用户积分
                userService.updateAllocationCredits(chartInfo.getUid(), interfaceInfo.getExpenses());
//                //用户的剩余调用数+1
//                userInterfaceInfoService.updateAllocationInvokeNum(BI_INTERFACE_ID, chartInfo.getUid(), 1);
            }
        }
    }
}
