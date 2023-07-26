package com.yeyou.yeyingBIbackend.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.common.ExcelToSQLEntity;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格处理工具类
 */
@Slf4j
public class ExcelUtils {

    public static ExcelToSQLEntity excelToString(MultipartFile multipartFile) {
        ExcelToSQLEntity excelToSQLEntity = new ExcelToSQLEntity();
        List<Map<Integer, String>> tableList = null;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            tableList = EasyExcel.read(inputStream)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("获取Excel输入流出现异常");
            ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR);
        }

        //获取字符串对象
        StringBuilder stringBuffer = new StringBuilder();
        //获取表头
        LinkedHashMap<Integer, String> headLinkedHashMap = (LinkedHashMap<Integer, String>) tableList.get(0);
        List<String> tableHead = headLinkedHashMap.values()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //设置表头
        excelToSQLEntity.setHeaders(tableHead);
        stringBuffer.append(StringUtils.join(tableHead, ',')).append('\n');
        //获取表格数据
        List<List<String>> valueList = new ArrayList<>();
        for (int i = 1; i < tableList.size(); i++) {
            //获取数据
            LinkedHashMap<Integer, String> dataLinkedHashMap = (LinkedHashMap<Integer, String>) tableList.get(i);
            List<String> tableData = dataLinkedHashMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            //设置属性
            valueList.add(tableData);
            stringBuffer.append(StringUtils.join(tableData, ',')).append('\n');
        }
        excelToSQLEntity.setValues(valueList);
        //去除最后的换行符
        String csv = stringBuffer.substring(0, stringBuffer.length() - 1);
        excelToSQLEntity.setStrCSV(csv);
        return excelToSQLEntity;
    }

    public static String parseStrToJavaTypeName(String str) {
        //转化为Char数组方便后续使用
        char[] charArray = str.toCharArray();
        //是否是Boolean类型
        if("true".equals(str) || "false".equals(str)) return "Boolean";
        int i;
        //小数点后最多两位
        int count=0;
        for (i = 0; i < charArray.length; i++) {
            if(charArray[i]=='.'){
                while(++i < charArray.length){
                    if(charArray[i]<'0' || charArray[i]>'9') break;
                    ++count;
                }
                break;
            } else if (charArray[i] < '0' || charArray[i] > '9') {
                //非数字
                break;
            }
        }
        if(i==charArray.length){
            if(count>=1 && count<=2) return "Double";
            else if(count==0) return "Long";
        }
        try {
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = fullDateFormat.parse(str);
            return "Date";
        } catch (ParseException e) {
            try {
                SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = fullDateFormat.parse(str);
                return "Date";
            } catch (ParseException ex) {
                return "String";
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(parseStrToJavaTypeName("12343534534534534"));    // 输出: Integer
        System.out.println(parseStrToJavaTypeName("2.1331"));  // 输出: Double
        System.out.println(parseStrToJavaTypeName("true"));  // 输出: Boolean
        System.out.println(parseStrToJavaTypeName("A"));     // 输出: String
        System.out.println(parseStrToJavaTypeName("2022-09-27 12:32:32"));     // 输出: String
        System.out.println(parseStrToJavaTypeName("2022-09-27"));     // 输出: String
    }

}
