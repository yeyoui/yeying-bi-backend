package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.LimitPresetConstant;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.bo.RedisRateLimitConfig;
import com.yeyou.yeyingBIbackend.service.RateLimitInfoService;
import com.yeyou.yeyingBIbackend.utils.StringRedisCacheUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class AIManager {
    @Resource
    private YuCongMingClient yuCongMingClient;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;
    @Resource
    private RateLimitInfoService rateLimitInfoService;

    public String doChat(long aiModel,String chartData){
        DevChatRequest devChatRequest = new DevChatRequest(aiModel, chartData);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> devChatResponseBaseResponse = yuCongMingClient.doChat(devChatRequest);
        //检查返回信息
        int retCode = devChatResponseBaseResponse.getCode();
        if(retCode !=0){
            if(retCode==500){
                //todo 加入缓存
                String redisKey = rateLimitInfoService.query().eq("limitPreset", LimitPresetConstant.BI_GEN_CHART).one().getRedisKey();
                //网络拥挤
                RedisRateLimitConfig redisRateLimitConfig = new RedisRateLimitConfig();
                redisRateLimitConfig.setRateInterval(1);
                redisRateLimitConfig.setRate(1);
                redisRateLimitConfig.setRedisKey(redisKey);
                stringRedisCacheUtils.set(RedisConstant.BI_RATE_LIMIT_RATE_KEY+1,redisRateLimitConfig,2, TimeUnit.MINUTES);
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,devChatResponseBaseResponse.getMessage());
        }

        return devChatResponseBaseResponse.getData().getContent();
    }
}
