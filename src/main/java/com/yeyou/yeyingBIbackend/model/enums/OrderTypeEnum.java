package com.yeyou.yeyingBIbackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 订单类型的枚举
 */
public enum OrderTypeEnum {
    NORMAL(0),
    COUPON(1);
    @EnumValue
    private final Integer code;

    OrderTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
