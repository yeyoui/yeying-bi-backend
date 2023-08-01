package com.yeyou.yeyingBIbackend.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.yeyou.yeyingBIbackend.common.ExcelToSQLEntity;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.UserChartInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author lhy
* @description 针对表【user_chart_info(用户表格元数据)】的数据库操作Service
* @createDate 2023-07-25 11:09:26
*/
@DS("userDS")
public interface UserChartInfoService extends IService<UserChartInfo> {
    /**
     * 为用户生成对应表格的数据表
     * @param excelToSQLEntity 提取的表格信息
     * @param chartInfo 用户请求和响应的信息
     */
    void createTable(ExcelToSQLEntity excelToSQLEntity, ChartInfo chartInfo);

    /**
     * 为用户生成对应表格的数据表插入数据
     * @param excelToSQLEntity 提取的表格信息
     * @param tableName 表名
     */
    void insertToUserTable(ExcelToSQLEntity excelToSQLEntity, String tableName);

    /**
     * 获取用户图表CSV数据
     * @param chartId
     */
    String getChartDataCSV(long chartId);

    /**
     * 根据图表ID获取用户图表的所有数据
     * @param chartId
     * @return
     */
    List<Map<String,String>> getChartDataByTableId(long chartId);

}
