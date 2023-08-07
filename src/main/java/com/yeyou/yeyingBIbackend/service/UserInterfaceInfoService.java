package com.yeyou.yeyingBIbackend.service;

import com.yeyou.yeyingBIbackend.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author lhy
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
 * @createDate 2023-08-04 11:05:03
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    /**
     * 校验
     *
     * @param userInterfaceInfo 接口id
     * @param add               是否为创建校验
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);


    /**
     * 接口调用扣额
     *
     * @param interfaceId 接口id
     * @param userId      用户id
     * @return 是否成功
     */
    boolean invokeDeduction(long interfaceId, long userId);

    /**
     * 检查用户调用额度
     * @param interfaceId 接口id
     * @param userId      用户id
     */
    void validUserInvolveQuota(long interfaceId, long userId);

    /**
     * 更新用户接口调用数
     *
     * @param interfaceId 接口id
     * @param userId      用户id
     * @param diff        新增或减少的调用数
     */
    void updateAllocationInvokeNum(long interfaceId, long userId, int diff);
}
