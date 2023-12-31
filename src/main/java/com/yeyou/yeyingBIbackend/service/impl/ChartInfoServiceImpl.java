package com.yeyou.yeyingBIbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.dto.chartInfo.ChartInfoQueryRequest;
import com.yeyou.yeyingBIbackend.model.dto.chartInfo.GenChartByAiResponse;
import com.yeyou.yeyingBIbackend.model.entity.*;
import com.yeyou.yeyingBIbackend.model.vo.ChartInfoVO;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.mapper.ChartInfoMapper;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.ParseChartResultUtil;
import com.yeyou.yeyingBIbackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;

/**
 * @author lhy
 * @description 针对表【chart_info(图标信息表)】的数据库操作Service实现
 * @createDate 2023-07-04 09:37:15
 */
@Service
public class ChartInfoServiceImpl extends ServiceImpl<ChartInfoMapper, ChartInfo>
        implements ChartInfoService {
    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Override
    public void validChartInfo(ChartInfo chartInfo, boolean add) {
        if (chartInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String chartType = chartInfo.getChartType();
        String goal = chartInfo.getGoal();
        // 有参数则校验
        //校验图表类型是否正确
        if (StringUtils.isBlank(chartType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表类型错误");
        }
        //校验是否设置目的，如果未设置则取默认值
        if (StringUtils.isBlank(goal)) {
            chartInfo.setGoal("请分析一下");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ChartInfo> getQueryWrapper(ChartInfoQueryRequest chartInfoQueryRequest, boolean onlyOwn) {
        QueryWrapper<ChartInfo> queryWrapper = new QueryWrapper<>();
        if (chartInfoQueryRequest == null) {
            return queryWrapper;
        }
        //获取用户信息
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        ThrowUtils.throwIf(requestAttributes == null, ErrorCode.NOT_LOGIN_ERROR);
        User loginUser = userService.getLoginUser(requestAttributes.getRequest());
        //获取ChartInfoQueryRequest对象的内容
        Long id = chartInfoQueryRequest.getId();
        String goal = chartInfoQueryRequest.getGoal();
        String name = chartInfoQueryRequest.getName();
        String sortField = chartInfoQueryRequest.getSortField();
        String sortOrder = chartInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        //1.按照图表ID查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        //2.按照图标名称模糊查询
        queryWrapper.like(ObjectUtils.isNotEmpty(name), "name", name);
        //3.按照用户自己ID查询
        if (onlyOwn) {
            queryWrapper.eq("uid", loginUser.getId());
        } else {
            ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.FORBIDDEN_ERROR);
        }
        //4.按照目的查询
        if (StringUtils.isNotBlank(goal)) {
            queryWrapper.like("goal", goal);
        }
        //5.图表存在
        queryWrapper.eq("isDelete", false);
        //6.排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}
