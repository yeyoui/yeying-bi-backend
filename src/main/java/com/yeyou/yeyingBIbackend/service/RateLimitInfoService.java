package com.yeyou.yeyingBIbackend.service;

import com.yeyou.yeyingBIbackend.model.entity.RateLimitInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lhy
* @description 针对表【rate_limit_info(接口限流表)】的数据库操作Service
* @createDate 2023-08-10 11:41:28
*/
public interface RateLimitInfoService extends IService<RateLimitInfo> {
    /**
     * 校验信息
     * @param rateLimitInfo 限流信息
     * @param add 是否是新增秦秋
     */
    void validRateLimitInfo(RateLimitInfo rateLimitInfo, boolean add);
}
