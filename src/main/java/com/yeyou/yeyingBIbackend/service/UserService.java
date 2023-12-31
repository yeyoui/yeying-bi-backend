package com.yeyou.yeyingBIbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyingBIbackend.model.dto.user.DailySignStatus;
import com.yeyou.yeyingBIbackend.model.dto.user.UserQueryRequest;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.vo.LoginUserVO;
import com.yeyou.yeyingBIbackend.model.vo.UserVO;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 接口调用扣额
     *
     * @param interfaceId 接口id
     * @param userId      用户id
     * @return 是否成功
     */
    void invokeDeduction(long interfaceId, long userId);

    /**
     * 更新用户积分
     *
     * @param userId      用户id
     * @param diff        新增或减少的调用数
     */
    void updateAllocationCredits(long userId, int diff);


    //-------------------------签到

    /**
     * 执行签到
     *
     * @param userId  用户ID
     * @param dateStr 查询的日期，默认当天 yyyy-MM-dd
     * @param doSign  操作是签到为true，否则为查询签到情况
     * @return
     */
    DailySignStatus dailySign(Long userId, String dateStr, boolean doSign);


}
