package com.yeyou.yeyingBIbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 接口限流表
 * @TableName rate_limit_info
 */
@TableName(value ="rate_limit_info")
@Data
public class RateLimitInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
