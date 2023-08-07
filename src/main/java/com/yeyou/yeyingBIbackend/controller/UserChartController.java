package com.yeyou.yeyingBIbackend.controller;

import com.yeyou.yeyingBIbackend.common.BaseResponse;
import com.yeyou.yeyingBIbackend.common.ResultUtils;
import com.yeyou.yeyingBIbackend.model.entity.UserChartInfo;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/userChart")
@Profile({"dev", "local"})
public class UserChartController {

    @Resource
    private UserChartInfoService userChartInfoService;

    @GetMapping("/getHeader")
    public BaseResponse<List<Map<String,String>>> getHeader(long chartId){
        UserChartInfo userChartInfo = userChartInfoService.getById(chartId);
        String fieldsName = userChartInfo.getFieldsName();
        ArrayList<Map<String, String>> mapArrayList = new ArrayList<>();
        for (String s : fieldsName.split(",")) {
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("title",s);
            resultMap.put("dataIndex",s);
            resultMap.put("editable","true");
            mapArrayList.add(resultMap);
        }
        return ResultUtils.success(mapArrayList);
    }

    @GetMapping("/getChartInfo")
    public BaseResponse<List<Map<String,String>>> getChartInfo(long chartId){
        AtomicInteger cnt= new AtomicInteger();
        List<Map<String, String>> chartDataByTableMap = userChartInfoService.getChartDataByTableId(chartId);
        //设置key
        chartDataByTableMap.forEach(item->{
            item.put("key", String.valueOf(cnt.getAndIncrement()));
        });
        return ResultUtils.success(chartDataByTableMap);
    }
}
