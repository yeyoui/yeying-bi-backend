package com.yeyou.yeyingBIbackend.model.dto.orderRecord.userInterfaceInfo;

import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求参数
 *
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {
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
     * 状态
     */
    private OrderStatusEnum status;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer surplusNum;
}
