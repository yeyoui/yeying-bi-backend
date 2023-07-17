package com.yeyou.yeyingBIbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图标信息表
 * @TableName chart_info
 */
@TableName(value ="chart_info")
@Data
public class ChartInfo implements Serializable {
    /**
     * 图表id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

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

    /**
     * 生成结果
     */
    private String genResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
