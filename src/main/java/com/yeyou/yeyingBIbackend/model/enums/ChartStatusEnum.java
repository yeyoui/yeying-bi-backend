package com.yeyou.yeyingBIbackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum ChartStatusEnum {

    WAIT(0),
    EXEC(1),
    SUCCESS(2),
    FAIL(3);

    @EnumValue
    private final int code;

    ChartStatusEnum(int code) {
        this.code = code;
    }
}
