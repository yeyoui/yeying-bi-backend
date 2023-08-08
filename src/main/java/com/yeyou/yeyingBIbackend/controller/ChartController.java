package com.yeyou.yeyingBIbackend.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyingBIbackend.annotation.AuthCheck;
import com.yeyou.yeyingBIbackend.model.vo.ChartInfoVO;
import com.yeyou.yeyingBIbackend.mq.BiMessageProducer;
import com.yeyou.yeyingBIbackend.common.*;
import com.yeyou.yeyingBIbackend.constant.CommonConstant;
import com.yeyou.yeyingBIbackend.constant.FileConstant;
import com.yeyou.yeyingBIbackend.constant.RedisConstant;
import com.yeyou.yeyingBIbackend.constant.UserConstant;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.manager.AIManager;
import com.yeyou.yeyingBIbackend.manager.RedisOps;
import com.yeyou.yeyingBIbackend.manager.RedissonRateLimiterManager;
import com.yeyou.yeyingBIbackend.model.dto.chartInfo.*;
import com.yeyou.yeyingBIbackend.model.entity.ChartInfo;
import com.yeyou.yeyingBIbackend.model.entity.User;
import com.yeyou.yeyingBIbackend.model.enums.ChartStatusEnum;
import com.yeyou.yeyingBIbackend.model.enums.FileUploadBizEnum;
import com.yeyou.yeyingBIbackend.service.ChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserChartInfoService;
import com.yeyou.yeyingBIbackend.service.UserInterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import com.yeyou.yeyingBIbackend.utils.ExcelUtils;
import com.yeyou.yeyingBIbackend.utils.NetUtils;
import com.yeyou.yeyingBIbackend.utils.ParseChartResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
    private AIManager aiManager;
    @Resource
    private UserChartInfoService userChartInfoService;
    @Resource
    private UserService userService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;
    @Resource
    private RedissonRateLimiterManager rateLimiterManager;
    @Resource
    private RedisOps redisOps;
    @Value("${yeying.BI_INTERFACE_ID}")
    private long BI_INTERFACE_ID;
    /**
     * AI模型ID
     */
    private static long aiModel= CommonConstant.BI_CHART_ANALYZE_ID;


    /**
     * 创建
     *
     * @param chartInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChartInfo>> listChartInfoVOByPage(@RequestBody ChartInfoQueryRequest chartInfoQueryRequest,
            HttpServletRequest request) {
        long current = chartInfoQueryRequest.getCurrent();
        long size = chartInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChartInfo> chartInfoPage = chartInfoService.page(new Page<>(current, size),
                chartInfoService.getQueryWrapper(chartInfoQueryRequest,false));
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
    public BaseResponse<Page<ChartInfoVO>> listMyChartInfoVOByPage(@RequestBody ChartInfoQueryRequest chartInfoQueryRequest,
            HttpServletRequest request) {
        if (chartInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = chartInfoQueryRequest.getCurrent();
        long size = chartInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ChartInfo> chartInfoPage = chartInfoService.page(new Page<>(current, size),
                chartInfoService.getQueryWrapper(chartInfoQueryRequest,true));
        Page<ChartInfoVO> chartInfoVOPage = new Page<>(current, size,chartInfoPage.getTotal());
        //处理表格数据
        List<ChartInfoVO> chartInfoVOList = chartInfoPage
                .getRecords()
                .stream()
                .map(chartInfo -> {
                    GenChartByAiResponse resultAndChartCode = ParseChartResultUtil.getResultAndChartCode(chartInfo.getGenResult());
                    ChartInfoVO chartInfoVO = new ChartInfoVO();
                    BeanUtils.copyProperties(chartInfo, chartInfoVO);
                    chartInfoVO.setGenResult(resultAndChartCode.getGenResult());
                    chartInfoVO.setChartDataJson(resultAndChartCode.getChartJsCode());
                    return chartInfoVO;
                }).collect(Collectors.toList());
        chartInfoVOPage.setRecords(chartInfoVOList);
        return ResultUtils.success(chartInfoVOPage);
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
        //用户限流
        rateLimiterManager.doRateLimiter(loginUser.getId().toString());
        //计费
        userInterfaceInfoService.invokeDeduction(BI_INTERFACE_ID, loginUser.getId());
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
        String aiRowAnswer = aiManager.doChat(aiModel, chartData);
        //返回信息（去除换行符)
        String genResult = aiRowAnswer.replaceAll("\n","");
        //封装返回对象
        GenChartByAiResponse aiResponse = ParseChartResultUtil.getResultAndChartCode(genResult);

        //新增数据到数据库
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setUid(loginUser.getId());
        chartInfo.setGoal(userGoal);
        chartInfo.setName(chartName);
        chartInfo.setChartType(chartType);
        chartInfo.setGenResult(genResult);
        chartInfo.setStatus(ChartStatusEnum.SUCCESS);
        chartInfoService.save(chartInfo);
        //为用户生成数据库表
        userChartInfoService.createTable(excelToSQLEntity,chartInfo);

        aiResponse.setId(chartInfo.getId());
        return ResultUtils.success(aiResponse);
    }

//    /**
//     * 分析用户上传文件（异步处理）
//     *
//     * @param multipartFile
//     * @param genChartByAiRequest 用户AI请求
//     * @param request
//     * @return
//     */
//    @PostMapping("/genChartByAiAS/async")
//    public BaseResponse<GenChartByAiResponse> genChartByAiASAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
//        //获取当前用户信息
//        User loginUser = userService.getLoginUser(request);
//        String userGoal = genChartByAiRequest.getUserGoal();
//        String chartName = genChartByAiRequest.getChartName();
//        String chartType = genChartByAiRequest.getChartType();
//        //用户限流
//        rateLimiterManager.doRateLimiter(loginUser.getId().toString());
//        //校验请求信息
//        ThrowUtils.throwIf(userGoal==null,ErrorCode.PARAMS_ERROR,"目标为空");
//        ThrowUtils.throwIf(chartName==null,ErrorCode.PARAMS_ERROR,"图表名称为空");
//        ThrowUtils.throwIf(chartType==null,ErrorCode.PARAMS_ERROR,"图表类型为空");
//        //校验文件信息
//        //文件不能过大
//        long size = multipartFile.getSize();
//        ThrowUtils.throwIf(size> FileConstant.MAX_EXCEL_FILE_SIZE,ErrorCode.PAYLOAD_LARGE_ERROR);
//        String originalFilename = multipartFile.getOriginalFilename();
//        String fileSuffix = FileUtil.getSuffix(originalFilename);
//        ThrowUtils.throwIf(!FileConstant.ACCEPTED_SUFFIX_LIST.contains(fileSuffix),ErrorCode.PAYLOAD_LARGE_ERROR);
//        //拼接提问信息
//        StringBuilder aiRequestMsg = new StringBuilder();
//        ExcelToSQLEntity excelToSQLEntity = ExcelUtils.excelToString(multipartFile);
//        aiRequestMsg.append(userGoal).append(",图表的类型是").append(chartType).append("\n").append(excelToSQLEntity.getStrCSV());
//        String chartData = aiRequestMsg.toString();
//
//        //新增数据到数据库（默认状态是等待中）
//        ChartInfo chartInfo = new ChartInfo();
//        chartInfo.setUid(loginUser.getId());
//        chartInfo.setGoal(userGoal);
//        chartInfo.setName(chartName);
//        chartInfo.setChartType(chartType);
//        chartInfoService.save(chartInfo);
//        //为用户生成数据库表
//        userChartInfoService.createTable(excelToSQLEntity,chartInfo);
//        //用于更新任务状态
//        ChartInfo updateChartInfo = new ChartInfo();
//        updateChartInfo.setId(chartInfo.getId());
//        //todo 异步处理
//        try {
//            CompletableFuture.runAsync(()->{
//                //设置请求进入队列处理
//                boolean updateSucceed = chartInfoService.update()
//                        .set("status",ChartStatusEnum.EXEC)
//                        .eq("id", chartInfo.getId()).update();
//                ThrowUtils.throwIf(!updateSucceed,ErrorCode.SYSTEM_ERROR,"系统更新状态失败");
//                //将请求发给AI处理
//                String aiRowAnswer = aiManager.doChat(aiModel, chartData);
//                //返回信息（去除换行符)
//                String genResult = aiRowAnswer.replaceAll("\n","");
////                //封装返回对象
////                GenChartByAiResponse aiResponse = ParseChartResultUtil.getResultAndChartCode(genResult);
////                aiResponse.setId(chartInfo.getId());
//                chartInfo.setGenResult(genResult);
//                //更新信息
//                updateSucceed = chartInfoService.update()
//                        .set("genResult", genResult)
//                        .set("status",ChartStatusEnum.SUCCESS)
//                        .eq("id", chartInfo.getId()).update();
//                ThrowUtils.throwIf(!updateSucceed,ErrorCode.SYSTEM_ERROR,"系统更新AI结果失败");
//            },threadPoolExecutor);
//        } catch (BusinessException ex){
//            //设置为调用失败
//            //todo 后续加入定时任务重试
//            updateChartInfo.setStatus(ChartStatusEnum.FAIL);
//            updateChartInfo.setExecMessage(ex.getMessage());
//            boolean errorSaveSucceed = chartInfoService.updateById(updateChartInfo);
//            if(!errorSaveSucceed){
//                log.error("设置保存图表错误状态：{}，出现异常，错误信息：",updateChartInfo.getId(),ex);
//            }
//        }
//        GenChartByAiResponse aiResponse = new GenChartByAiResponse();
//        aiResponse.setId(chartInfo.getId());
//        return ResultUtils.success(aiResponse);
//    }


    /**
     * 分析用户上传文件（异步MQ处理）
     *
     * @param multipartFile
     * @param genChartByAiRequest 用户AI请求
     * @param request
     * @return
     */
    @PostMapping("/genChartByAiAS/async/mq")
    public BaseResponse<GenChartByAiResponse> genChartByAiASAsyncMq(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //获取当前用户信息
        User loginUser = userService.getLoginUser(request);
        String userGoal = genChartByAiRequest.getUserGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();
        //用户限流
        rateLimiterManager.doRateLimiter(loginUser.getId().toString());
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
        //计费
        userInterfaceInfoService.validUserInvolveQuota(BI_INTERFACE_ID,loginUser.getId());
        userInterfaceInfoService.invokeDeduction(BI_INTERFACE_ID,loginUser.getId());
        ExcelToSQLEntity excelToSQLEntity = ExcelUtils.excelToString(multipartFile);

        //新增数据到数据库（默认状态是等待中）
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setUid(loginUser.getId());
        chartInfo.setGoal(userGoal);
        chartInfo.setName(chartName);
        chartInfo.setChartType(chartType);
        chartInfoService.save(chartInfo);
        //为用户生成数据库表
        userChartInfoService.createTable(excelToSQLEntity,chartInfo);
        //将用户请求发送到消息队列中
        //todo 加入发送确认机制
        biMessageProducer.sendMsgToBI(chartInfo.getId().toString());
        //设置请求进入队列处理
        boolean updateSucceed = chartInfoService.update()
                .set("status", ChartStatusEnum.EXEC)
                .eq("id", chartInfo.getId()).update();
        ThrowUtils.throwIf(!updateSucceed,ErrorCode.SYSTEM_ERROR,"系统更新状态失败");
        //返回信息
        GenChartByAiResponse aiResponse = new GenChartByAiResponse();
        aiResponse.setId(chartInfo.getId());
        return ResultUtils.success(aiResponse);
    }

    @GetMapping("/genChartStatus")
    public SseEmitter getGenChartStatus(){
        SseEmitter emitter = new SseEmitter(0L); // 创建SseEmitter对象
        //获取用户信息
        User loginUser = userService.getLoginUser(NetUtils.getHttpServletRequest());
        String queueName= RedisConstant.BI_NOTIFY_UID+loginUser.getId();
        // 在后台线程中发送事件
        new Thread(() -> {
            try {
                // 发送事件
                while (true){
                    String s = redisOps.dequeueBlock(queueName);
                    if(!s.equals("null")) {
                        emitter.send(SseEmitter.event().data(s).name("event1"));
                    }
                    else{
                        //等待10秒重试
                        Thread.sleep(10000);
                    }
                }
            } catch (Exception e) {
                emitter.completeWithError(e); // 发送出错时，完成发送
            }finally {
                emitter.complete();  // 完成发送
            }
        }).start();
        return emitter;
    }

    /**
     * 重试图表生成
     * @param chartId 图表ID
     */
    @GetMapping("/reSubmitChart")
    public BaseResponse reSubmitChart(Long chartId){
        //用户限流
        User loginUser = userService.getLoginUser(NetUtils.getHttpServletRequest());
        rateLimiterManager.doRateLimiter(loginUser.getId().toString());
        //todo 权限校验
        userInterfaceInfoService.invokeDeduction(BI_INTERFACE_ID, loginUser.getId());
        //更新图表状态
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setId(chartId);
        chartInfo.setStatus(ChartStatusEnum.EXEC);
        //直接放入消息队列
        biMessageProducer.sendMsgToBI(chartId.toString());
        return ResultUtils.success("加入队列成功");
    }

    @GetMapping("/retEmpty")
    public String retEmpty(){
        return null;
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
