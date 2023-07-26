package com.yeyou.yeyingBIbackend.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;

//@SpringBootTest
public class ChatGPTTest {

    @Resource
//    private YuCongMingClient client;

    @Test
    public void testChat(){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1681133195828957186L);
        devChatRequest.setMessage("你还记得你的设定吗\n");
//        BaseResponse<DevChatResponse> devChatResponseBaseResponse = client.doChat(devChatRequest);
//        System.out.println(devChatResponseBaseResponse.getData());
    }

    @Test
    public void chatWithGPT(){
        HashMap<String, String> headMap = new HashMap<>();
        headMap.put("Content-Type", "application/json");
        headMap.put("Authorization", "Bearer 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");

        HashMap<String, String> bodyMap = new HashMap<>();
        bodyMap.put("model","gpt-3.5-turbo");
        bodyMap.put("message","[{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\", \"content\": \"Hello!\"}]");
        HttpResponse execute = HttpRequest.post("https://api.openai.com/v1/chat/completions")
                .body(bodyMap.toString())
                .addHeaders(headMap)
                .execute();
        System.out.println(execute.body());
    }


}
