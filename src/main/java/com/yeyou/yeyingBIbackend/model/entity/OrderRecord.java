package com.yeyou.yeyingBIbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.OrderTypeEnum;
import lombok.Data;

/**
 * 用户调用接口关系
 * @TableName order_record
 */
@TableName(value ="order_record")
@Data
public class OrderRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 订单名
     */
    private String orderName;

    /**
     * 第三方支付交易流水号
     */
    private String outPayNo;

    /**
     * 第三方支付渠道编号
     */
    private String outPayChannel;

    /**
     * 订单状态 0-未支付 1-等待系统处理 2-支付成功 3-支付失败
     */
    private OrderStatusEnum status;

    /**
     * 订单类型 0-正常 1-优惠券
     */
    private OrderTypeEnum orderType;

    /**
     * 购买的总调用次数
     */
    private Integer totalNum;

    /**
     * 订单总额（单位分）
     */
    private Long totalPrice;

    /**
     * 支付成功时间
     */
    private Date paySuccessTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删  1-以删
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
