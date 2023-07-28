package com.yeyou.yeyingBIbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yeyou.yeyingBIbackend.model.dto.chartInfo.ChartInfoQueryRequest;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 图表服务
*/
public interface ChartInfoService extends IService<ChartInfo> {

    /**
     * 校验
     *
     * @param chartInfo
     * @param add
     */
    void validChartInfo(ChartInfo chartInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @param onlyOwn
     * @return
     */
    QueryWrapper<ChartInfo> getQueryWrapper(ChartInfoQueryRequest postQueryRequest,boolean onlyOwn);
}
