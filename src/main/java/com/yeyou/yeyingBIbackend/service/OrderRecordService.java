package com.yeyou.yeyingBIbackend.service;

import cn.hutool.core.lang.Pair;
import com.yeyou.yeyingBIbackend.model.entity.OrderRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyingBIbackend.model.entity.UserInterfaceInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author lhy
* @description 针对表【order_record(用户调用接口关系)】的数据库操作Service
* @createDate 2023-08-04 11:05:03
*/
public interface OrderRecordService extends IService<OrderRecord> {
    /**
     * 校验更新好新增请求
     * @param orderRecord 订单信息
     * @param add 是否是新增请求
     */
    void validOrderRecord(OrderRecord orderRecord, boolean add);

    /**
     * 创建订单
     * @param orderRecord
     * @return
     */
    long createOrder(OrderRecord orderRecord);

    /**
     * 根据接口ID计算价格
     * @param interfaceInfos 接口id和购买数量的pair
     * @return 计算好的价格(单位 分)
     */
    long calculatePrice(List<Pair<Long,Integer>> interfaceInfos);

    /**
     * 获取支付二维码
     * @param orderRecordId 订单ID
     * @return 二维码的代码信息
     */
    String getPaymentQR(long orderRecordId);
}
