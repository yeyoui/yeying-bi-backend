package com.yeyou.yeyingBIbackend.config;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.yeyou.yeyingBIbackend.mapper.UserChartInfoMapper;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

//@SpringBootTest
public class SQLTest {


//    @Resource
//    private UserService userService;
//    @Resource
//    private UserChartInfoService userChartInfoService;
//    @Resource
//    private UserChartInfoMapper userChartInfoMapper;

//    @Test
//    public void testTowDatabase(){
//        String sql = "CREATE TABLE test01(name varchar(64),age int)";
//        userChartInfoMapper.createUserTable(sql);
////        System.out.println(userChartInfoService.list().get(0));
////        System.out.println(userService.getById(1676054884748668930L));
////        System.out.println("_________________________");
////        xueshengService.selectAll().forEach(System.out::println);
//
//    }

    @Test
    public void testTypeToSQL() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();

        System.out.println(list);
    }
}
