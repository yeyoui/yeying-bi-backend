package com.yeyou.yeyingBIbackend.manager;

import cn.hutool.core.lang.func.VoidFunc0;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sse管理类
 */
@Slf4j
public class SseEmitterManager {

    private static Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    /**
     * 创建Sse连接
     * @param userId
     * @return
     */
    public static SseEmitter connect(Long userId){
        try {
            SseEmitter sseEmitter = new SseEmitter(0L);
            //注册回调方法
            Runnable removeUserCallBack = () -> removeUser(userId);
            sseEmitter.onCompletion(removeUserCallBack);
            sseEmitter.onTimeout(removeUserCallBack);
            sseEmitter.onError(throwable->{
                log.error("Sse连接异常,原因:{}",throwable.getMessage());
            });
            sseEmitterMap.put(userId, sseEmitter);
            return sseEmitter;
        } catch (Exception e) {
            log.error("创建新的sse连接异常，当前用户：{}", userId,e);
        }
        return null;
    }

    public static void sendMessage(Long userId,String message,String type){
        log.info("uid:{}->Sse连接成功",userId);
        if(sseEmitterMap.containsKey(userId)){
            try {
                sseEmitterMap.get(userId).send(SseEmitter.event().data(message).name(type));
            } catch (IOException e) {
                log.error("用户[{}]推送异常:{}", userId, e.getMessage());
                removeUser(userId);
            }
        }
    }

    /**
     * 移除指定用户的Sse
     * @param userId
     */
    private static void removeUser(Long userId){
        sseEmitterMap.remove(userId);
    }
}
