package com.yeyou.yeyingBIbackend.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL 工具
 *
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }


    private static final Map<String, String> typeNameMap;

    static {
        typeNameMap = new HashMap<>();
        typeNameMap.put("String", "varchar(128)");
        typeNameMap.put("byte[]", "blob");
        typeNameMap.put("Long", "BIGINT");
        typeNameMap.put("Integer", "int");
        typeNameMap.put("java.math.BigInteger", "bigint");
        typeNameMap.put("Float", "float");
        typeNameMap.put("Double", "double");
        typeNameMap.put("BigDecimal", "decimal");
        typeNameMap.put("Boolean", "boolean");
        typeNameMap.put("Date", "datetime");
        typeNameMap.put("Time", "time");
        typeNameMap.put("Timestamp", "timestamp");
    }

    /**
     * 转化成SQL类型
     * @param javaTypeName 类型名
     * @return
     */
    public static String getMySQLTypeName(String javaTypeName) {
        return typeNameMap.getOrDefault(javaTypeName, null);
    }

    public static void main(String[] args) {
        Timestamp javaTypeName = new Timestamp(1212);
        String typeName1 = javaTypeName.getClass().getSimpleName();
        System.out.println(javaTypeName.getClass().getName()+" "+typeName1 + " " + getMySQLTypeName(typeName1));
    }
}
