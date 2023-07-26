package com.yeyou.yeyingBIbackend.utils;

import com.yeyou.yeyingBIbackend.model.dto.chartInfo.GenChartByAiResponse;

import java.util.stream.Stream;

public class ParseChartResultUtil {
    public static GenChartByAiResponse getResultAndChartCode(String aiResult){
        GenChartByAiResponse response=new GenChartByAiResponse();
        String[] split = aiResult.split("&&&&&");
        response.setChartJsCode(split[1]);
        response.setGenResult(split[2]);
        return response;
    }

//    public static void main(String[] args) {
//        String data = "&&&&&option = {    xAxis: {        type: 'category',        data: ['第一天', '第二天', '第三天', '第四天', '第五天', '第六天']    },    yAxis: {        type: 'value'    },    series: [{        data: [2589, 3200, 2800, 3444, 4565, 3567],        type: 'bar'    }]};&&&&&根据提供的数据生成的柱状图显示了您钱包的金额情况。从图中可以看出，第四天的金额最高，达到了3444。之前的几天金额相对较低，在2000到3000之间波动。从第五天开始，金额开始增加，最后一天的金额为3567。整体来说，您的钱包金额呈现了上升的趋势，特别是第四天有一个明显的峰值。这可能表示您在这一天进行了较大的消费或收入。同时，第一天的金额较低，可能是因为您在这一天进行了较大的支出或收入较少。需要进一步分析数据背后的原因和趋势，以便做出更合理的财务规划。";
//        GenChartByAiResponse resultAndChartCode = getResultAndChartCode(data);
//        System.out.println(resultAndChartCode);
//    }
}
