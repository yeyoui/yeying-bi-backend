package com.yeyou.yeyingBIbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表格元数据
 * @TableName user_chart_info
 */
@TableName(value ="user_chart_info")
@Data
public class UserChartInfo implements Serializable {
    /**
     * 图表id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 行数
     */
    private Integer rowNum;

    /**
     * 表格对应的字段名称
     */
    private String fieldsName;

    /**
     * 列数
     */
    private Integer columnNum;

    /**
     * 前一次生成的图表Id
     */
    private Long parentId;

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
