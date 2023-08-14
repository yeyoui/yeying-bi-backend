package com.yeyou.yeyingBIbackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 接口状态枚举
 */
public enum InterfaceStatusEnum {

    BAN(0),
    FIX(1),
    NORMAL(2);
    @EnumValue
    final int code;

    InterfaceStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
