package com.yeyou.yeyingBIbackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 订单状态的枚举
 */
public enum OrderStatusEnum {
    UNPAID(0),
    PROCESSING(1),
    SUCCESS(2),
    FAILURE(3);
    @EnumValue
    private final Integer code;

    OrderStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
