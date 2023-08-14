package com.yeyou.yeyingBIbackend.model.dto.user;

import lombok.Data;

/**
 * 用户签到情况
 */
@Data
public class DailySignStatus {
    private int continuousDay;
    private Long totalDay;
    private Boolean todaySigned;
}
