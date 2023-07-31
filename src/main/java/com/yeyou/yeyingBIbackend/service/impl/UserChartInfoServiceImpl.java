package com.yeyou.yeyingBIbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ExcelToSQLEntity;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.UserChartInfo;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import com.yeyou.yeyingBIbackend.mapper.UserChartInfoMapper;
import com.yeyou.yeyingBIbackend.utils.ExcelUtils;
import com.yeyou.yeyingBIbackend.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author lhy
* @description 针对表【user_chart_info(用户表格元数据)】的数据库操作Service实现
* @createDate 2023-07-25 11:09:26
*/
@Service
public class UserChartInfoServiceImpl extends ServiceImpl<UserChartInfoMapper, UserChartInfo>
    implements UserChartInfoService{
    @Resource
    UserChartInfoMapper userChartInfoMapper;
    private static String CREATE_TABLE_PREFIX = "CREATE TABLE ";
    private static String INSERT_TABLE_PREFIX = "INSERT INTO ";
    private static String USER_UPLOAD_TABLE_PREFIX="user_table_";
    private static String SELECT_TABLE_PREFIX="SELECT * FROM ";

    @Override
    public void createTable(ExcelToSQLEntity excelToSQLEntity, ChartInfo chartInfo) {
        StringBuilder stringBuilder = new StringBuilder(CREATE_TABLE_PREFIX);
        String tableName=USER_UPLOAD_TABLE_PREFIX+chartInfo.getId();
        stringBuilder.append(tableName).append("(");
        //转化为数组来高效遍历
        ArrayList<String> headers = new ArrayList<>(excelToSQLEntity.getHeaders());
        ArrayList<String> firstContent = new ArrayList<>(excelToSQLEntity.getValues().get(0));

        //添加字段
        for (int i = 0; i < headers.size(); i++) {
            String javaType=ExcelUtils.parseStrToJavaTypeName(firstContent.get(i));
            stringBuilder
                    .append(headers.get(i))
                    .append(" ")
                    .append(SqlUtils.getMySQLTypeName(javaType));
            if(i!= headers.size()-1){
                stringBuilder.append(",");
            }
            //插入的字符串处理
            if("String".equals(javaType)){
                //todo 性能优化
                //每一列都要处理
                for (List<String> aRow : excelToSQLEntity.getValues()) {
                    aRow.set(i,"'"+aRow.get(i)+"'");
                }
            }
        }
        stringBuilder.append(")");
        //新增表(注意SQL注入风险)
        userChartInfoMapper.createUserTable(stringBuilder.toString());
        //新增表记录
        UserChartInfo userChartInfo = new UserChartInfo();
        userChartInfo.setId(chartInfo.getId());
        userChartInfo.setColumnNum(excelToSQLEntity.getHeaders().size());
        userChartInfo.setRowNum(excelToSQLEntity.getValues().size());
        userChartInfo.setFieldsName(StringUtils.join(excelToSQLEntity.getHeaders(),','));
        this.save(userChartInfo);
        //插入数据(注意SQL注入风险)
        insertToUserTable(excelToSQLEntity,tableName);
    }

    @Override
    public void insertToUserTable(ExcelToSQLEntity excelToSQLEntity,  String tableName) {
        StringBuilder stringBuilder = new StringBuilder(INSERT_TABLE_PREFIX+tableName+" VALUES");
        for (List<String> value : excelToSQLEntity.getValues()) {
            stringBuilder.append("(").append(StringUtils.join(value, ",")).append("),");
        }
        userChartInfoMapper.insertToUserTable(stringBuilder.substring(0,stringBuilder.length()-1));
    }

    @Override
    public String getChartDataCSV(long chartId) {
        //获取表头信息
        UserChartInfo chartInfo = this.query().select("fieldsName").eq("id", chartId).one();
        String fieldsName = chartInfo.getFieldsName();
        StringBuilder stringBuilder = new StringBuilder(fieldsName+'\n');
        //获取数据
        List<HashMap<String,String>> chartMapList = userChartInfoMapper.getChartDataByTableName(USER_UPLOAD_TABLE_PREFIX + chartId);
        //获取字段数组
        String[] split = StringUtils.split(fieldsName, ',');
        for (HashMap<String, String> aRow : chartMapList) {
            StringBuilder rowBuilder = new StringBuilder();
            for (String s : split) {
                rowBuilder.append(String.valueOf(aRow.get(s))).append(',');
            }
            rowBuilder.append("\n");
            stringBuilder.append(rowBuilder);
        }
        System.out.println(stringBuilder);
        return stringBuilder.toString();
    }

}
