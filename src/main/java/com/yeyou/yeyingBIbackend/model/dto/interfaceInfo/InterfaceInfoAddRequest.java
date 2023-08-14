package com.yeyou.yeyingBIbackend.model.dto.interfaceInfo;

import com.yeyou.yeyingBIbackend.model.enums.OrderTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口添加请求参数
 *
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    private static final long serialVersionUID = 2504395398821302448L;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口描述
     */
    private String interfaceDescribe;
}
