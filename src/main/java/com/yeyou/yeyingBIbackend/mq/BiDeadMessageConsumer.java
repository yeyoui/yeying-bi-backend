package com.yeyou.yeyingBIbackend.mq;

import com.rabbitmq.client.Channel;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.manager.AIManager;
import com.yeyou.yeyingBIbackend.manager.RedisOps;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.OrderRecord;
import com.yeyou.yeyingBIbackend.model.enums.ChartStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.service.OrderRecordService;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 死信队列消费者
 */
@Component
@Slf4j
public class BiDeadMessageConsumer {
    @Resource
    private ChartInfoService chartInfoService;
    @Resource
    private UserChartInfoService userChartInfoService;
    @Resource
    private OrderRecordService orderRecordService;
    @Resource
    private AIManager aiManager;
    @Resource
    private RedisOps redisOps;

    @RabbitListener(queues = BiMqConstant.BI_DEAD_QUEUE,ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        //处理任务
        //用于更新任务状态
        ChartInfo updateChartInfo=new ChartInfo();
        ChartInfo chartInfo = null;
        try {
            //获取用户上传的图表信息
            long chartId = Long.parseLong(message);
            chartInfo = chartInfoService.getById(chartId);
            //图表必须存在
            ThrowUtils.throwIf(chartInfo.getId()==null,ErrorCode.PARAMS_ERROR);
            updateChartInfo.setId(chartInfo.getId());
            //从数据库中获取表格数据
            String chartDataCSV = userChartInfoService.getChartDataCSV(chartId);
            StringBuilder aiRequestMsg = new StringBuilder();
            aiRequestMsg.append(chartInfo.getGoal()).append(",图表的类型是").append(chartInfo.getChartType()).append("\n").append(chartDataCSV);
            //将请求发给AI处理
            String aiRowAnswer = aiManager.doChat(CommonConstant.BI_CHART_ANALYZE_ID, aiRequestMsg.toString());
            //返回信息（去除换行符)
            String genResult = aiRowAnswer.replaceAll("\n","");
            chartInfo.setGenResult(genResult);
            //检查任务是否已经完成（防止重复消费）
            ChartStatusEnum status = chartInfoService.query().select("status").eq("id", chartId).one().getStatus();
            if(!ChartStatusEnum.SUCCESS.equals(status)){
                //更新信息
                boolean updateSucceed = chartInfoService.update()
                        .set("genResult", genResult)
                        .set("status",ChartStatusEnum.SUCCESS)
                        .set("execMessage",null)
                        .eq("id", chartInfo.getId()).update();
                ThrowUtils.throwIf(!updateSucceed,ErrorCode.SYSTEM_ERROR,"系统更新AI结果失败");
                //发送消息给用户
                String responseMsg=String.format("您的表格[%s:%d]，分析成功！", chartInfo.getName(),chartInfo.getId());
                String queueName= RedisConstant.BI_NOTIFY_UID+chartInfo.getUid();
                redisOps.enqueue(queueName,responseMsg);
            }
        } catch (BusinessException | NumberFormatException ex) {
            if(chartInfo==null){
                log.error("获取图表失败");
                return;
            }
            //发送消息给用户
            String responseMsg=String.format("您的表格[%s:%d]，分析失败！原因，%s", chartInfo.getName(),chartInfo.getId(), ex.getMessage());
            String queueName= RedisConstant.BI_NOTIFY_UID+chartInfo.getUid();
            redisOps.enqueue(queueName,responseMsg);
            //设置为调用失败
            updateChartInfo.setStatus(ChartStatusEnum.FAIL);
            updateChartInfo.setExecMessage(ex.getMessage());
            boolean errorSaveSucceed = chartInfoService.updateById(updateChartInfo);
            if(!errorSaveSucceed){
                log.error("设置保存图表错误状态：{}，出现异常，错误信息：",updateChartInfo.getId(),ex);
            }
        }
        //ACK消息
        ackCurMessage(channel, deliveryTag);
    }

    @RabbitListener(queues = BiMqConstant.ORDER_DEAD_QUEUE,ackMode = "MANUAL")
    public void receiveOrderFailMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        //获取订单ID
        long orderId = Long.parseLong(message);
        //查询订单ID
        try {
            OrderRecord orderRecord = orderRecordService.getById(orderId);
            if(orderRecord==null){
                ThrowUtils.throwIf(true,ErrorCode.SYSTEM_ERROR,"从消息队列获取订单失败,订单ID:"+message);
            }
            //如果订单状态是未支付则将订单设置为失败
            if (OrderStatusEnum.UNPAID.equals(orderRecord.getStatus())) {
                boolean succeed = orderRecordService
                        .update().set("status", OrderStatusEnum.FAILURE)
                        .eq("id",orderId)
                        .update();
                ThrowUtils.throwIf(!succeed,ErrorCode.SYSTEM_ERROR,"更新订单状态失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            ackCurMessage(channel, deliveryTag);
        }
    }

    private static void ackCurMessage(Channel channel, long deliveryTag) {
        //ACK消息
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            log.error("消息确认失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
