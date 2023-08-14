package com.yeyou.yeyingBIbackend.model.dto.orderRecord;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.OrderTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 更新请求参数
 *
 */
@Data
public class OrderRecordUpdateRequest implements Serializable {
    private static final long serialVersionUID = -2992930400039826800L;
    /**
     * 主键
     */
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
}
