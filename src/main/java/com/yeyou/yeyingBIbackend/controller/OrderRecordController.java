package com.yeyou.yeyingBIbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyingBIbackend.annotation.AuthCheck;
import com.yeyou.yeyingBIbackend.common.BaseResponse;
import com.yeyou.yeyingBIbackend.common.DeleteRequest;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.common.ResultUtils;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.model.dto.userInterfaceInfo.OrderRecordAddRequest;
import com.yeyou.yeyingBIbackend.model.dto.userInterfaceInfo.OrderRecordQueryRequest;
import com.yeyou.yeyingBIbackend.model.dto.userInterfaceInfo.OrderRecordUpdateRequest;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.entity.OrderRecord;
import com.yeyou.yeyingBIbackend.mq.BiMessageProducer;
import com.yeyou.yeyingBIbackend.service.OrderRecordService;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 订单记录接口
 *
 * @author yeyou
 */
@RestController
@RequestMapping("/orderRecord")
@Slf4j
public class OrderRecordController {

    @Resource
    private OrderRecordService orderRecordService;
    @Resource
    private UserService userService;
    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 创建
     *
     * @param orderRecordAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addOrderRecord(@RequestBody OrderRecordAddRequest orderRecordAddRequest, HttpServletRequest request) {
        if (orderRecordAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderRecord orderRecord = new OrderRecord();
        BeanUtils.copyProperties(orderRecordAddRequest, orderRecord);
        // 校验
        orderRecordService.validOrderRecord(orderRecord, true);
        User loginUser = userService.getLoginUser(request);
        orderRecord.setUserId(loginUser.getId());
        boolean result = orderRecordService.save(orderRecord);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newOrderRecordId = orderRecord.getId();
        return ResultUtils.success(newOrderRecordId);
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
    public BaseResponse<Boolean> deleteOrderRecord(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        OrderRecord oldOrderRecord = orderRecordService.getById(id);
        if (oldOrderRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldOrderRecord.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = orderRecordService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param orderRecordUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateOrderRecord(@RequestBody OrderRecordUpdateRequest orderRecordUpdateRequest,
                                            HttpServletRequest request) {
        if (orderRecordUpdateRequest == null || orderRecordUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderRecord orderRecord = new OrderRecord();
        BeanUtils.copyProperties(orderRecordUpdateRequest, orderRecord);
        // 参数校验
        orderRecordService.validOrderRecord(orderRecord, false);
        User user = userService.getLoginUser(request);
        long id = orderRecordUpdateRequest.getId();
        // 判断是否存在
        OrderRecord oldOrderRecord = orderRecordService.getById(id);
        if (oldOrderRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅管理员可修改
        boolean result = orderRecordService.updateById(orderRecord);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<OrderRecord> getOrderRecordById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderRecord orderRecord = orderRecordService.getById(id);
        return ResultUtils.success(orderRecord);
    }

    /**
     * 获取列表（仅管理员可使用）
     * @param orderRecordQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<OrderRecord>> listOrderRecord(OrderRecordQueryRequest orderRecordQueryRequest) {
        OrderRecord orderRecordQuery = new OrderRecord();
        if (orderRecordQueryRequest != null) {
            BeanUtils.copyProperties(orderRecordQueryRequest, orderRecordQuery);
        }
        QueryWrapper<OrderRecord> queryWrapper = new QueryWrapper<>(orderRecordQuery);
        List<OrderRecord> orderRecordList = orderRecordService.list(queryWrapper);
        return ResultUtils.success(orderRecordList);
    }

    /**
     * 分页获取列表
     *
     * @param orderRecordQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<OrderRecord>> listOrderRecordByPage(OrderRecordQueryRequest orderRecordQueryRequest, HttpServletRequest request) {
        if (orderRecordQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderRecord orderRecordQuery = new OrderRecord();
        BeanUtils.copyProperties(orderRecordQueryRequest, orderRecordQuery);
        long current = orderRecordQueryRequest.getCurrent();
        long size = orderRecordQueryRequest.getPageSize();
        String sortField = orderRecordQueryRequest.getSortField();
        String sortOrder = orderRecordQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<OrderRecord> queryWrapper = new QueryWrapper<>(orderRecordQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<OrderRecord> orderRecordPage = orderRecordService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(orderRecordPage);
    }

    @PostMapping("/genOrderAndRetQR")
    public BaseResponse<String> genOrderAndRetQR(@RequestBody OrderRecordAddRequest orderRecordAddRequest){
        if(orderRecordAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        OrderRecord orderRecord = new OrderRecord();
        BeanUtils.copyProperties(orderRecordAddRequest,orderRecord);
        //设置用户信息
        HttpServletRequest httpServletRequest = NetUtils.getHttpServletRequest();
        User loginUser = userService.getLoginUser(httpServletRequest);
        orderRecord.setUserId(loginUser.getId());
        long orderRecordId = orderRecordService.createOrder(orderRecord);
        //生成下单二维码
        String paymentQR = orderRecordService.getPaymentQR(orderRecordId);
        //订单15分钟自动取消
        biMessageProducer.sendMsgToDelayOrder(String.valueOf(orderRecordId));
        return ResultUtils.success(paymentQR);
    }
}
