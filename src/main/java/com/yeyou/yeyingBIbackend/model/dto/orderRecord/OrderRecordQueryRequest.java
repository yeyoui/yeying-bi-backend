package com.yeyou.yeyingBIbackend.model.dto.orderRecord;

import com.yeyou.yeyingBIbackend.common.PageRequest;
import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.OrderTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口查询请求参数
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderRecordQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 471278761377557210L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private Long userName;

    /**
     * 接口名
     */
    private Long interfaceName;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 订单名
     */
    private String orderName;

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
}
