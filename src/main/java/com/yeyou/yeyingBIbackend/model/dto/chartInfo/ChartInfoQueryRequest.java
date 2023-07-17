package com.yeyou.yeyingBIbackend.model.dto.chartInfo;

import com.yeyou.yeyingBIbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 图表id
     */
    private Long id;
    /**
     * 目的
     */
    private String goal;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 要生成的表格类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
