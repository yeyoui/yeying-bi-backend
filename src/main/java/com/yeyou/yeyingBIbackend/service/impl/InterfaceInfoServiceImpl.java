package com.yeyou.yeyingBIbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.entity.InterfaceInfo;
import com.yeyou.yeyingBIbackend.model.enums.InterfaceStatusEnum;
import com.yeyou.yeyingBIbackend.service.InterfaceInfoService;
import com.yeyou.yeyingBIbackend.mapper.InterfaceInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author lhy
 * @description 针对表【interface_info(接口信息表)】的数据库操作Service实现
 * @createDate 2023-08-11 15:02:55
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        //空参数
        if (interfaceInfo == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long id = interfaceInfo.getId();
        String interfaceName = interfaceInfo.getInterfaceName();
        Integer expenses = interfaceInfo.getExpenses();

        InterfaceInfo oldInterfaceInfo = this.getById(id);
        //如果是更新操作，id必须存在
        ThrowUtils.throwIf(!add && oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "接口id错误");
        //如果执行的是新增
        if(add){
            //接口名称必须存在
            ThrowUtils.throwIf(StringUtils.isBlank(interfaceName),ErrorCode.PARAMS_ERROR,"接口名不能为空");
        }
        //调用的价格必须大于等于0
        if(expenses!=null && expenses<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "积分费用必须大于等于0");
        }
        //接口名称不能过长
        if(!StringUtils.isBlank(interfaceName) && interfaceName.length()>256){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
    }
}




