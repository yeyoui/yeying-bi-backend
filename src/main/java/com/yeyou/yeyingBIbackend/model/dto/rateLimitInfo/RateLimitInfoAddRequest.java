package com.yeyou.yeyingBIbackend.model.dto.rateLimitInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 */
@Data
public class RateLimitInfoAddRequest implements Serializable {

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * Redis中的键名
     */
    private String redisKey;

    /**
     * 限流预设值
     */
    private Integer limitPreset;

    /**
     * 区间内科执行的次数
     */
    private Integer rate;

    /**
     * 时间区间
     */
    private Integer rateInterval;

    private static final long serialVersionUID = 1L;
}
