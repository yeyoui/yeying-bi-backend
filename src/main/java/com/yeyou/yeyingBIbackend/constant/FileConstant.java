package com.yeyou.yeyingBIbackend.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件常量
 *
 */
public interface FileConstant {

    /**
     * 表格文件最大10M
     */
    long MAX_EXCEL_FILE_SIZE=1024*1024*10;

    /**
     * 表格文件可接受的后缀
     */
    ArrayList<String> ACCEPTED_SUFFIX_LIST = new ArrayList<>(Arrays.asList("xlsx", "xls"));
}
