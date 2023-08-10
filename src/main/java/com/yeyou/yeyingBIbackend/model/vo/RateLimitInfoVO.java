package com.yeyou.yeyingBIbackend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口限流VO
 */
@TableName(value ="rate_limit_info")
@Data
public class RateLimitInfoVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 接口名
     */
    private String interfaceName;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删  1-已删
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
