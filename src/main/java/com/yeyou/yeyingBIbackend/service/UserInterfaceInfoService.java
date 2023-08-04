package com.yeyou.yeyingBIbackend.service;

import com.yeyou.yeyingBIbackend.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author lhy
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
 * @createDate 2023-08-04 11:05:03
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    /**
     * 校验
     *
     * @param userInterfaceInfo
     * @param add               是否为创建校验
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);


    /**
     * 接口调用计数
     *
     * @param interfaceId
     * @param userId
     * @return 是否成功
     */
    boolean invokeCount(long interfaceId, long userId);
}
