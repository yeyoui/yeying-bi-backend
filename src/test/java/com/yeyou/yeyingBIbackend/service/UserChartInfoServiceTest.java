package com.yeyou.yeyingBIbackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserChartInfoServiceTest {
    @Resource
    private UserChartInfoService userChartInfoService;
    @Test
    void getChartDataCSV() {
        userChartInfoService.getChartDataCSV(1684011962268147713L);
    }
}
