package com.yeyou.yeyingBIbackend.model.dto.chartInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 */
@Data
public class ChartInfoAddRequest implements Serializable {

    /**
     * 目的
     */
    private String goal;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 表格数据
     */
    private String chartData;

    /**
     * 要生成的表格类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
