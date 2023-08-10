package com.yeyou.yeyingBIbackend.model.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis逻辑过期对象
 */
@Data
public class RedisData {
    private Object data;
    private LocalDateTime expireTime;
}
