package com.yeyou.yeyingBIbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.entity.RateLimitInfo;
import com.yeyou.yeyingBIbackend.service.RateLimitInfoService;
import com.yeyou.yeyingBIbackend.mapper.RateLimitInfoMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author lhy
 * @description 针对表【rate_limit_info(接口限流表)】的数据库操作Service实现
 * @createDate 2023-08-10 11:41:28
 */
@Service
public class RateLimitInfoServiceImpl extends ServiceImpl<RateLimitInfoMapper, RateLimitInfo>
        implements RateLimitInfoService {
    @Override
    public void validRateLimitInfo(RateLimitInfo rateLimitInfo, boolean add) {
        if (rateLimitInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = rateLimitInfo.getId();
        Long interfaceId = rateLimitInfo.getInterfaceId();
        String redisKey = rateLimitInfo.getRedisKey();
        Integer limitPreset = rateLimitInfo.getLimitPreset();
        Integer rate = rateLimitInfo.getRate();
        Integer rateInterval = rateLimitInfo.getRateInterval();

        RateLimitInfo editRateLimitInfo = null;
        //id存在
        if (!add) {
            ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "ID错误");
            editRateLimitInfo = this.getById(id);
            ThrowUtils.throwIf(editRateLimitInfo == null, ErrorCode.PARAMS_ERROR, "ID错误");
        }
        // 创建时，参数不能为空
        //todo 接口信息校验
        //redisKey不能重复
        RateLimitInfo redisKeyS1 = this.query().eq("redisKey", redisKey).one();
        if (redisKeyS1 != null && (add || !id.equals(redisKeyS1.getId()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "redisKey已经存在");
        }
        //limitPreset不能重复
        RateLimitInfo limitPresetS1 = this.query().eq("limitPreset", limitPreset).one();
        if (limitPresetS1 != null && (add || !id.equals(limitPresetS1.getId()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "limitPreset已经存在");
        }
        //rate大于0
        ThrowUtils.throwIf(rate!=null && rate<=0,ErrorCode.PARAMS_ERROR,"rate错误");
        //rateInterval 大于0
        ThrowUtils.throwIf(rateInterval!=null && rateInterval<=0,ErrorCode.PARAMS_ERROR,"rateInterval错误");
    }
}




