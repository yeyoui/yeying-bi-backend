package com.yeyou.yeyingBIbackend.model.dto.chartInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 */
@Data
public class GenChartByAiResponse implements Serializable {
    /**
     * 图表id
     */
    private Long id;

    /**
     * 生成结果
     */
    private String genResult;

    /**
     * 调表代码
     */
    private String chartJsCode;

    private static final long serialVersionUID = 1L;
}
