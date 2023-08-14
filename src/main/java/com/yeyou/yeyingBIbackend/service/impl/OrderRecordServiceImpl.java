package com.yeyou.yeyingBIbackend.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.exception.ThrowUtils;
import com.yeyou.yeyingBIbackend.model.entity.InterfaceInfo;
import com.yeyou.yeyingBIbackend.model.entity.OrderRecord;
import com.yeyou.yeyingBIbackend.model.entity.UserInterfaceInfo;
import com.yeyou.yeyingBIbackend.service.InterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.OrderRecordService;
import com.yeyou.yeyingBIbackend.mapper.OrderRecordMapper;
import com.yeyou.yeyingBIbackend.utils.QRCodeUtil;
import com.yeyou.yeyingBIbackend.utils.RedisIdWorker;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.yeyou.yeyingBIbackend.config.AlipayConfig.CHARSET;

/**
 * @author lhy
 * @description 针对表【order_record(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-08-04 11:05:03
 */
@Service
public class OrderRecordServiceImpl extends ServiceImpl<OrderRecordMapper, OrderRecord>
        implements OrderRecordService {

    @Resource
    private RedisIdWorker redisIdWorker;
    @Value("${yeying.BI_INTERFACE_ID}")
    private Long BI_INTERFACE_ID;
    @Value("${pay.alipay.ALPAY_QR_ADDR}")
    String ALPAY_QR_ADDR;
    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public void validOrderRecord(OrderRecord orderRecord, boolean add) {
        //空参数
        if (orderRecord == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //如果是更新操作，则ID不能为空
        Long id = orderRecord.getId();
        if (!add && (id == null || id < 0 || this.getById(id) == null)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ID错误");
        }
        Long userId = orderRecord.getUserId();
        Long interfaceId = orderRecord.getInterfaceId();
        if (userId < 0 || interfaceId < 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的ID");
        //todo 查询接口是否存在(暂时只有一个接口)
        orderRecord.setInterfaceId(BI_INTERFACE_ID);
        //在新增订单时，如果订单名不存在，设定为用户ID：未定义的订单：时间戳
        if (add && (StringUtils.isBlank(orderRecord.getOrderName()))) {
            String orderName = userId + ":未定义的订单:" + redisIdWorker.nextId("order:" + userId + ":");
            orderRecord.setOrderName(orderName);
        }
        //购买数量必须大于0
        Integer totalNum = orderRecord.getTotalNum();
        if (totalNum == null || totalNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "购买数量必须大于0");
        }
    }

    @Override
    public long createOrder(OrderRecord orderRecord) {
        //校验参数是否正确
        this.validOrderRecord(orderRecord, true);
        //计算总价
        long totalPrice = this.calculatePrice(orderRecord.getTotalNum(),orderRecord.getInterfaceId());
        ThrowUtils.throwIf(totalPrice<0,ErrorCode.SYSTEM_ERROR,"价格异常");
        orderRecord.setTotalPrice(totalPrice);
        //新增订单
        boolean addSucceed = this.save(orderRecord);
        ThrowUtils.throwIf(!addSucceed, ErrorCode.SYSTEM_ERROR);
        return orderRecord.getId();
    }

    @Override
    public long calculatePrice(long num,long interfaceId) {
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
        long sum = 0L;
        Integer price = interfaceInfo.getExpenses();
        sum += (num) * price;
        return sum;
    }

    @Override
    public String getPaymentQR(long orderRecordId) {
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String qrCode;
        String paymentUrl = ALPAY_QR_ADDR + orderRecordId;
        try {
            qrCode = qrCodeUtil.createQRCode(paymentUrl, 400, 400);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return qrCode;
    }

    @Override
    public String getPaymentURL(long orderRecordId) {
        return ALPAY_QR_ADDR + orderRecordId;
    }
}




