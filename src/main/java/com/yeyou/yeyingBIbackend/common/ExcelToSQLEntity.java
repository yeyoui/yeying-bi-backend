package com.yeyou.yeyingBIbackend.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExcelToSQLEntity {
    /**
     * 字段名
     */
    private List<String> headers;
    /**
     * 插入数据
     */
    private List<List<String>> values;
    /**
     * CSV格式
     */
    private String strCSV;
}
