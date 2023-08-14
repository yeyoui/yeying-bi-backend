package com.yeyou.yeyingBIbackend.model.dto.interfaceInfo;

import com.yeyou.yeyingBIbackend.common.PageRequest;
import com.yeyou.yeyingBIbackend.model.enums.InterfaceStatusEnum;
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
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 471278761377557210L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 状态 0-禁用 1-维护 2-正常
     */
    private InterfaceStatusEnum status;

    /**
     * 费用
     */
    private Integer expenses;
}
