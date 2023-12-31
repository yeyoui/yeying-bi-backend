package com.yeyou.yeyingBIbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.entity.UserInterfaceInfo;
import com.yeyou.yeyingBIbackend.service.UserInterfaceInfoService;
import com.yeyou.yeyingBIbackend.mapper.UserInterfaceInfoMapper;
import com.yeyou.yeyingBIbackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lhy
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-08-04 11:05:03
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private UserService userService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        //空参数
        if (userInterfaceInfo == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //如果执行的是更新操作
        if (add) {
            Long userId = userInterfaceInfo.getUserId();
            Long interfaceId = userInterfaceInfo.getInterfaceId();
            if (userId < 0 || interfaceId < 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的ID");
            if (userInterfaceInfo.getSurplusNum() < 0)
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于0");
        }
    }


    @Override
    public boolean invokeDeduction(long interfaceId, long userId) {
        //查询接口和用户参数是否正确
        if (interfaceId < 0 || userId < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId", interfaceId).eq("userId", userId);
        updateWrapper.gt("surplusNum", 0);
        updateWrapper.setSql("totalNum=totalNum+1,surplusNum=surplusNum-1");
        return this.update(updateWrapper);
    }

    @Override
    public void validUserInvolveQuota(long interfaceId, long userId) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceId", interfaceId).eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo = this.getOne(queryWrapper);
        //查看是否被禁用
        ThrowUtils.throwIf(userInterfaceInfo.getStatus()==1,ErrorCode.FORBIDDEN_ERROR,"接口调用被禁用");
        //查看是否还有调用次数
        ThrowUtils.throwIf(userInterfaceInfo.getSurplusNum()<=0,ErrorCode.OPERATION_ERROR,"调用次数不足，请充值");
    }

    @Override
    public void updateAllocationInvokeNum(long interfaceId, long userId, int diff) {
        //查询接口和用户参数是否正确
        if (interfaceId < 0 || userId < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //查询接口绑定信息是否存在，如果不存在就直接创建
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceId", interfaceId);
        UserInterfaceInfo userInterfaceInfo = this.getOne(queryWrapper);
        if (userInterfaceInfo == null) {
            //新增绑定关系
            userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceId(interfaceId);
            userInterfaceInfo.setSurplusNum(diff);
            this.validUserInterfaceInfo(userInterfaceInfo, true);
            this.save(userInterfaceInfo);
            return;
        }
        //更新剩余调用数量
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userInterfaceInfo.getId());
        updateWrapper.setSql("surplusNum=surplusNum+" + diff);
        this.update(updateWrapper);
    }
}




