package com.yeyou.yeyingBIbackend.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.yeyou.yeyingBIbackend.model.entity.UserChartInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author lhy
* @description 针对表【user_chart_info(用户表格元数据)】的数据库操作Mapper
* @createDate 2023-07-25 11:09:26
* @Entity com.yeyou.yeyingBIbackend.model.entity.UserChartInfo
*/
@DS("userDS")
public interface UserChartInfoMapper extends BaseMapper<UserChartInfo> {

    void createUserTable(String sqlStatement);

    void insertToUserTable(String sqlStatement);

}




