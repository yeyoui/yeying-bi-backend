package com.yeyou.yeyingBIbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yeyou.yeyingBIbackend.annotation.AuthCheck;
import com.yeyou.yeyingBIbackend.common.BaseResponse;
import com.yeyou.yeyingBIbackend.common.DeleteRequest;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.common.ResultUtils;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.dto.rateLimitInfo.RateLimitInfoAddRequest;
import com.yeyou.yeyingBIbackend.model.dto.rateLimitInfo.RateLimitInfoEditRequest;
import com.yeyou.yeyingBIbackend.model.dto.rateLimitInfo.RateLimitInfoQueryRequest;
import com.yeyou.yeyingBIbackend.model.dto.rateLimitInfo.RateLimitInfoUpdateRequest;
import com.yeyou.yeyingBIbackend.model.entity.RateLimitInfo;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.vo.RateLimitInfoVO;
import com.yeyou.yeyingBIbackend.service.RateLimitInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖接口限流接口
 *
 */
@RestController
@RequestMapping("/rateLimitInfo")
@Slf4j
@Profile({"dev", "local"})
public class RateLimitInfoController {

    @Resource
    private RateLimitInfoService rateLimitInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     * @param rateLimitInfoAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addRateLimitInfo(@RequestBody RateLimitInfoAddRequest rateLimitInfoAddRequest) {
        if (rateLimitInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        RateLimitInfo rateLimitInfo = new RateLimitInfo();
        BeanUtils.copyProperties(rateLimitInfoAddRequest, rateLimitInfo);
        rateLimitInfoService.validRateLimitInfo(rateLimitInfo, true);
        boolean result = rateLimitInfoService.save(rateLimitInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newRateLimitInfoId = rateLimitInfo.getId();
        return ResultUtils.success(newRateLimitInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteRateLimitInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        RateLimitInfo oldRateLimitInfo = rateLimitInfoService.getById(id);
        ThrowUtils.throwIf(oldRateLimitInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = rateLimitInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param rateLimitInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateRateLimitInfo(@RequestBody RateLimitInfoUpdateRequest rateLimitInfoUpdateRequest) {
        if (rateLimitInfoUpdateRequest == null || rateLimitInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        RateLimitInfo rateLimitInfo = new RateLimitInfo();
        BeanUtils.copyProperties(rateLimitInfoUpdateRequest, rateLimitInfo);
        // 参数校验
        rateLimitInfoService.validRateLimitInfo(rateLimitInfo, false);
        long id = rateLimitInfoUpdateRequest.getId();
        // 判断是否存在
        RateLimitInfo oldRateLimitInfo = rateLimitInfoService.getById(id);
        ThrowUtils.throwIf(oldRateLimitInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = rateLimitInfoService.updateById(rateLimitInfo);
        return ResultUtils.success(result);
    }

//    /**
//     * 根据 id 获取
//     *
//     * @param id
//     * @return
//     */
//    @GetMapping("/get/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<RateLimitInfoVO> getRateLimitInfoVOById(long id, HttpServletRequest request) {
//        if (id <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        RateLimitInfo rateLimitInfo = rateLimitInfoService.getById(id);
//        if (rateLimitInfo == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        return ResultUtils.success(rateLimitInfoService.getRateLimitInfoVO(rateLimitInfo, request));
//    }

//    /**
//     * 分页获取列表（封装类）
//     *
//     * @param rateLimitInfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<RateLimitInfoVO>> listRateLimitInfoVOByPage(@RequestBody RateLimitInfoQueryRequest rateLimitInfoQueryRequest,
//            HttpServletRequest request) {
//        long current = rateLimitInfoQueryRequest.getCurrent();
//        long size = rateLimitInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<RateLimitInfo> rateLimitInfoPage = rateLimitInfoService.page(new Page<>(current, size),
//                rateLimitInfoService.getQueryWrapper(rateLimitInfoQueryRequest));
//        return ResultUtils.success(rateLimitInfoService.getRateLimitInfoVOPage(rateLimitInfoPage, request));
//    }
//
//    /**
//     * 分页获取当前用户创建的资源列表
//     *
//     * @param rateLimitInfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/my/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<RateLimitInfoVO>> listMyRateLimitInfoVOByPage(@RequestBody RateLimitInfoQueryRequest rateLimitInfoQueryRequest,
//            HttpServletRequest request) {
//        if (rateLimitInfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        rateLimitInfoQueryRequest.setUserId(loginUser.getId());
//        long current = rateLimitInfoQueryRequest.getCurrent();
//        long size = rateLimitInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<RateLimitInfo> rateLimitInfoPage = rateLimitInfoService.page(new Page<>(current, size),
//                rateLimitInfoService.getQueryWrapper(rateLimitInfoQueryRequest));
//        return ResultUtils.success(rateLimitInfoService.getRateLimitInfoVOPage(rateLimitInfoPage, request));
//    }

}
