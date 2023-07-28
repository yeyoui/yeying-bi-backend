package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AIManager {
    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChat(long aiModel,String chartData){
        DevChatRequest devChatRequest = new DevChatRequest(aiModel, chartData);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> devChatResponseBaseResponse = yuCongMingClient.doChat(devChatRequest);
        //检查返回信息
        ThrowUtils.throwIf(devChatResponseBaseResponse.getCode()!=0, ErrorCode.SYSTEM_ERROR,devChatResponseBaseResponse.getMessage());
        return devChatResponseBaseResponse.getData().getContent();
    }
}
