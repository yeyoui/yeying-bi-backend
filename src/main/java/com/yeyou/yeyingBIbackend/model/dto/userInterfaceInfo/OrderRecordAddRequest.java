package com.yeyou.yeyingBIbackend.model.dto.userInterfaceInfo;

import com.yeyou.yeyingBIbackend.model.enums.OrderTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口添加请求参数
 *
 */
@Data
public class OrderRecordAddRequest implements Serializable {

    private static final long serialVersionUID = 2504395398821302448L;

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
     * 订单类型 0-正常 1-优惠券
     */
    private OrderTypeEnum orderType;

    /**
     * 购买的总调用次数
     */
    private Integer totalNum;

}
