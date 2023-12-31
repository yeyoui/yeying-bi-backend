package com.yeyou.yeyingBIbackend.exception;

import com.yeyou.yeyingBIbackend.common.BaseResponse;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.RejectedExecutionException;

/**
 * 全局异常处理器
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RejectedExecutionException.class)
    public BaseResponse<?> runtimeExceptionHandler(RejectedExecutionException e) {
        log.error("RejectedExecutionException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统繁忙中");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
