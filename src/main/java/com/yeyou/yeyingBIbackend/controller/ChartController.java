package com.yeyou.yeyingBIbackend.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yeyou.yeyingBIbackend.annotation.AuthCheck;
import com.yeyou.yeyingBIbackend.common.*;
import com.yeyou.yeyingBIbackend.constant.FileConstant;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.dto.chartInfo.*;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.enums.FileUploadBizEnum;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.ExcelUtils;
import com.yeyou.yeyingBIbackend.utils.ParseChartResultUtil;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Random;

/**
 * 帖子接口
 *
 */
@RestController
@RequestMapping("/chartInfo")
@Slf4j
public class ChartController {

    @Resource
    private ChartInfoService chartInfoService;

    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private UserChartInfoService userChartInfoService;
    @Resource
    private UserService userService;

    @Value("${yuapi.modelId}")
    private long aiModel;

    private final static Gson GSON = new Gson();


    /**
     * 创建
     *
     * @param chartInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChartInfo(@RequestBody ChartInfoAddRequest chartInfoAddRequest, HttpServletRequest request) {
        if (chartInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChartInfo chartInfo = new ChartInfo();
        BeanUtils.copyProperties(chartInfoAddRequest, chartInfo);
        User loginUser = userService.getLoginUser(request);

        chartInfo.setUid(loginUser.getId());
        chartInfoService.validChartInfo(chartInfo, true);

        boolean result = chartInfoService.save(chartInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartInfoId = chartInfo.getId();
        return ResultUtils.success(newChartInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChartInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ChartInfo oldChartInfo = chartInfoService.getById(id);
        ThrowUtils.throwIf(oldChartInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChartInfo.getUid().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChartInfo(@RequestBody ChartInfoUpdateRequest chartInfoUpdateRequest) {
        if (chartInfoUpdateRequest == null || chartInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChartInfo chartInfo = new ChartInfo();
        BeanUtils.copyProperties(chartInfoUpdateRequest, chartInfo);
        // 参数校验
        chartInfoService.validChartInfo(chartInfo, false);
        long id = chartInfoUpdateRequest.getId();
        // 判断是否存在
        ChartInfo oldChartInfo = chartInfoService.getById(id);
        ThrowUtils.throwIf(oldChartInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartInfoService.updateById(chartInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartInfo> getChartInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChartInfo chartInfo = chartInfoService.getById(id);
        if (chartInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chartInfo);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChartInfo>> listChartInfoVOByPage(@RequestBody ChartInfoQueryRequest chartInfoQueryRequest,
            HttpServletRequest request) {
        long current = chartInfoQueryRequest.getCurrent();
        long size = chartInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChartInfo> chartInfoPage = chartInfoService.page(new Page<>(current, size),
                chartInfoService.getQueryWrapper(chartInfoQueryRequest));
        //处理表格数据
        chartInfoPage
                .getRecords()
                .forEach(chartInfo -> chartInfo
                        .setGenResult(ParseChartResultUtil.getResultAndChartCode(chartInfo.getGenResult()).getChartJsCode()));
        return ResultUtils.success(chartInfoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ChartInfo>> listMyChartInfoVOByPage(@RequestBody ChartInfoQueryRequest chartInfoQueryRequest,
            HttpServletRequest request) {
        if (chartInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //todo uid逻辑
//        chartInfoQueryRequest.setUid(loginUser.getId());
        long current = chartInfoQueryRequest.getCurrent();
        long size = chartInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChartInfo> chartInfoPage = chartInfoService.page(new Page<>(current, size),
                chartInfoService.getQueryWrapper(chartInfoQueryRequest));
        return ResultUtils.success(chartInfoPage);
    }

    /**
     * 编辑（用户）
     *
     * @param chartInfoEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChartInfo(@RequestBody ChartInfoEditRequest chartInfoEditRequest, HttpServletRequest request) {
        if (chartInfoEditRequest == null || chartInfoEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ChartInfo chartInfo = new ChartInfo();
        BeanUtils.copyProperties(chartInfoEditRequest, chartInfo);
        // 参数校验
        chartInfoService.validChartInfo(chartInfo, false);
        User loginUser = userService.getLoginUser(request);
        long id = chartInfoEditRequest.getId();
        // 判断是否存在
        ChartInfo oldChartInfo = chartInfoService.getById(id);
        ThrowUtils.throwIf(oldChartInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChartInfo.getUid().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartInfoService.updateById(chartInfo);
        return ResultUtils.success(result);
    }


    /**
     * 分析用户上传文件
     *
     * @param multipartFile
     * @param genChartByAiRequest 用户AI请求
     * @param request
     * @return
     */
    @PostMapping("/genChartByAi")
    public BaseResponse<GenChartByAiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //获取当前用户信息
        User loginUser = userService.getLoginUser(request);
        String userGoal = genChartByAiRequest.getUserGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();
        //校验请求信息
        ThrowUtils.throwIf(userGoal==null,ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(chartName==null,ErrorCode.PARAMS_ERROR,"图表名称为空");
        ThrowUtils.throwIf(chartType==null,ErrorCode.PARAMS_ERROR,"图表类型为空");
        //校验文件信息
        //文件不能过大
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size> FileConstant.MAX_EXCEL_FILE_SIZE,ErrorCode.PAYLOAD_LARGE_ERROR);
        String originalFilename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!FileConstant.ACCEPTED_SUFFIX_LIST.contains(fileSuffix),ErrorCode.PAYLOAD_LARGE_ERROR);

        //拼接提问信息
        StringBuilder aiRequestMsg = new StringBuilder();
        ExcelToSQLEntity excelToSQLEntity = ExcelUtils.excelToString(multipartFile);
        aiRequestMsg.append(userGoal).append(",图表的类型是").append(chartType).append("\n").append(excelToSQLEntity.getStrCSV());
        String chartData = aiRequestMsg.toString();
        //将请求发给AI处理
        DevChatRequest devChatRequest = new DevChatRequest(aiModel, chartData);
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> devChatResponseBaseResponse = yuCongMingClient.doChat(devChatRequest);
        //检查返回信息
        ThrowUtils.throwIf(devChatResponseBaseResponse.getCode()!=0,ErrorCode.SYSTEM_ERROR,devChatResponseBaseResponse.getMessage());
        //返回信息（去除换行符)
        String genResult = devChatResponseBaseResponse.getData().getContent().replaceAll("\n","");
        //封装返回对象
        GenChartByAiResponse aiResponse = ParseChartResultUtil.getResultAndChartCode(genResult);

        //新增数据到数据库
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setUid(loginUser.getId());
        chartInfo.setGoal(userGoal);
        chartInfo.setName(chartName);
        chartInfo.setChartData(excelToSQLEntity.getStrCSV());
        chartInfo.setChartType(chartType);
        chartInfo.setGenResult(genResult);
        chartInfoService.save(chartInfo);
        //为用户生成数据库表
//        //模拟ID
//        chartInfo.setId(RandomUtil.randomLong(1,10000000000L));
        userChartInfoService.createTable(excelToSQLEntity,chartInfo);

        aiResponse.setId(chartInfo.getId());
        return ResultUtils.success(aiResponse);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

}
