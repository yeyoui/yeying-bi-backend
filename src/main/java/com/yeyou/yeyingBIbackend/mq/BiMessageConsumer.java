package com.yeyou.yeyingBIbackend.mq;

import com.github.rholder.retry.*;
import com.rabbitmq.client.Channel;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.manager.AIManager;
import com.yeyou.yeyingBIbackend.manager.RedisOps;
import com.yeyou.yeyingBIbackend.manager.RedissonRateLimiterManager;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.enums.ChartStatusEnum;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 消息消费者
 */
@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartInfoService chartInfoService;
    @Resource
    private UserChartInfoService userChartInfoService;
    @Resource
    private AIManager aiManager;
    @Resource
    private RedisOps redisOps;
    @Resource
    private RedissonRateLimiterManager rateLimiterManager;

    /**
     * 自建线程池，用于管理所有AI任务
     */
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @RabbitListener(queues = BiMqConstant.BI_QUEUE, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        rateLimiterManager.doRateLimiter("BI分析图表任务",1,1);
        //异步处理
        CompletableFuture.runAsync(()->processMessage(message, channel, deliveryTag),threadPoolExecutor);
    }

    private void processMessage(String message, Channel channel, long deliveryTag) {
        //处理任务
        //用于更新任务状态
        ChartInfo updateChartInfo = new ChartInfo();
        try {
            //通过guava-retrying库来完成重试和超时管理
            Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                    .retryIfException()
                    //阻塞等待
                    .withBlockStrategy(BlockStrategies.threadSleepStrategy())
                    //按照斐波那列的值来重试等待
                    .withWaitStrategy(WaitStrategies.fibonacciWait(20,TimeUnit.SECONDS))
                    //任务超时管理
                    .withAttemptTimeLimiter(AttemptTimeLimiters.fixedTimeLimit(50,TimeUnit.SECONDS))
                    //最多重试次数
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();
            retryer.call(() -> proceedAITask(message, updateChartInfo));
        } catch (BusinessException | ExecutionException | RetryException ex) {
            //NACK消息
            try {
                //更新状态
                //拒绝消息后进入死信队列处理
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e) {
                log.error("消息确认失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return;
        } catch (NumberFormatException e) {
            log.error("解析图表ID：{}，出现异常，错误信息：", message, e);
        }
        //ACK消息
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息确认失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private boolean proceedAITask(String message, ChartInfo updateChartInfo) throws BusinessException{
        log.info("\n—————————————————正在处理BI任务—————————————————");
        //获取用户上传的图表信息
        long chartId = Long.parseLong(message);
        ChartInfo chartInfo = chartInfoService.getById(chartId);
        updateChartInfo.setId(chartInfo.getId());
        //从数据库中获取表格数据
        String chartDataCSV = userChartInfoService.getChartDataCSV(chartId);
        StringBuilder aiRequestMsg = new StringBuilder();
        aiRequestMsg.append(chartInfo.getGoal()).append(",图表的类型是").append(chartInfo.getChartType()).append("\n").append(chartDataCSV);
        //将请求发给AI处理
        String aiRowAnswer = aiManager.doChat(CommonConstant.BI_CHART_ANALYZE_ID, aiRequestMsg.toString());
        //返回信息（去除换行符)
        String genResult = aiRowAnswer.replaceAll("\n", "");
        chartInfo.setGenResult(genResult);
        //检查任务是否已经完成（防止重复消费）
        ChartStatusEnum status = chartInfoService.query().select("status").eq("id", chartId).one().getStatus();
        if(ChartStatusEnum.SUCCESS.equals(status)){
            //无需再处理
            return true;
        }
        //更新信息
        boolean updateSucceed = chartInfoService.update()
                .set("genResult", genResult)
                .set("status", ChartStatusEnum.SUCCESS)
                .eq("id", chartInfo.getId()).update();
        ThrowUtils.throwIf(!updateSucceed, ErrorCode.SYSTEM_ERROR, "系统更新AI结果失败");
        //发送消息给用户
        String responseMsg = String.format("您的表格[%s:%d]，分析成功！", chartInfo.getName(), chartInfo.getId());
        String queueName = RedisConstant.BI_NOTIFY_UID + chartInfo.getUid();
        redisOps.enqueue(queueName, responseMsg);
        return true;
    }
}
