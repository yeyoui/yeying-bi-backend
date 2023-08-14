package com.yeyou.yeyingBIbackend.model.dto.interfaceInfo;

import com.yeyou.yeyingBIbackend.model.enums.InterfaceStatusEnum;
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
public class InterfaceInfoUpdateRequest implements Serializable {
    private static final long serialVersionUID = -2992930400039826800L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口描述
     */
    private String interfaceDescribe;

    /**
     * 状态 0-禁用 1-维护 2-正常
     */
    private InterfaceStatusEnum status;

    /**
     * 费用
     */
    private Integer expenses;

}
