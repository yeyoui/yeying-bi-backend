package com.yeyou.yeyingBIbackend.service;

import com.yeyou.yeyingBIbackend.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lhy
* @description 针对表【interface_info(接口信息表)】的数据库操作Service
* @createDate 2023-08-11 15:02:56
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     * @param interfaceInfo 接口信息
     * @param add 是否是新增请求
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
