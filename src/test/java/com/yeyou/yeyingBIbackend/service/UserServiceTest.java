package com.yeyou.yeyingBIbackend.service;

import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Value("${yeying.BI_INTERFACE_ID}")
    private long BI_INTERFACE_ID;

    @Test
    void userRegister() {
        String userAccount = "yeyoui";
        String userPassword = "";
        String checkPassword = "123456";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }

    @Test
    void invokeDeductionTest(){
        userService.invokeDeduction(BI_INTERFACE_ID, 1689471849894793218L);
    }
}
