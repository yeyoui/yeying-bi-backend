package com.yeyou.yeyingBIbackend.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.config.AlipayConfig;
import com.yeyou.yeyingBIbackend.exception.BusinessException;
import com.yeyou.yeyingBIbackend.model.entity.OrderRecord;
import com.yeyou.yeyingBIbackend.model.enums.OrderStatusEnum;
import com.yeyou.yeyingBIbackend.service.OrderRecordService;
import com.yeyou.yeyingBIbackend.service.UserInterfaceInfoService;
import com.yeyou.yeyingBIbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static com.alipay.api.AlipayConstants.SIGN_TYPE;
import static com.yeyou.yeyingBIbackend.config.AlipayConfig.CHARSET;

/**
 * 用于处理支付请求
 */
@Controller
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Resource
    private OrderRecordService orderRecordService;
    @Resource
    private UserService userService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;
    @Value("${pay.alipay.ALPAY_QR_ADDR}")
    String ALPAY_QR_ADDR;
    @Value("${yeying.SERVER_HOST}")
    private String SERVER_HOST;

    @GetMapping("/getPaymentView")
    public void getPaymentView(@RequestParam("orderRecordId") Long orderRecordId,HttpServletResponse httpResponse) {
        //获取订单信息
        OrderRecord orderRecord = orderRecordService.getById(orderRecordId);
        if(orderRecord==null){
            handlerError(httpResponse, "支付服务获取订单号时出现异常");
            return;
        } else if (!OrderStatusEnum.UNPAID.equals(orderRecord.getStatus())) {
            OrderStatusEnum status = orderRecord.getStatus();
            String retMsg;
            switch(status){
                case PROCESSING:
                    retMsg = "订单正在处理";
                    break;
                case FAILURE:
                    retMsg = "订单已失败，请重新下单";
                    break;
                case SUCCESS:
                    retMsg = "订单已支付成功";
                    break;
                default:
                    retMsg = "未知异常";
            }
            handlerError(httpResponse, retMsg);
            return;
        }
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY,
                AlipayConfig.FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        //异步接收地址，仅支持http/https，公网可访问
        request.setReturnUrl(SERVER_HOST+"/api/payment/toPaymentSucceedPage");
        request.setNotifyUrl(SERVER_HOST+"/api/payment/paymentResult");//在公共参数中设置回跳和通知地址
        //同步跳转地址，仅支持http/https
        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderRecordId);
        //支付金额，最小值0.01元
        BigDecimal bigDecimal = new BigDecimal(orderRecord.getTotalPrice());
        String totalPrice = bigDecimal.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString();
        bizContent.put("total_amount", totalPrice);
        //订单标题，不可使用特殊符号
        bizContent.put("subject", orderRecord.getOrderName());

        //跳转信息
        bizContent.put("return_url", SERVER_HOST+"/api/payment/getPaymentQR");
        //退出支付
        bizContent.put("quit_url", SERVER_HOST+"/api/payment/getPaymentQR");

        /******可选参数******/
        //手机网站支付默认传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
//        bizContent.put("time_expire", "2022-08-01 22:00:00");

        //// 商品明细信息，按需传入
//        JSONArray goodsDetail = new JSONArray();
//        JSONObject goods1 = new JSONObject();
//        goods1.put("goods_id", "goodsNo1");
//        goods1.put("goods_name", "子商品1");
//        goods1.put("quantity", 1);
//        goods1.put("price", 0.01);
//        goodsDetail.add(goods1);
//        bizContent.put("goods_detail", goodsDetail);

        //更新订单状态
        //支付宝处理返回信息
        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (response.isSuccess()) {
            String input = response.getBody();
            httpResponse.setContentType("text/html;charset=" + CHARSET);
            try {
                httpResponse.getWriter().write(input);//直接将完整的表单html输出到页面
                httpResponse.getWriter().flush();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            System.out.println("调用失败");
        }
    }

    /**
     * 将错误信息传给用户
     * @param httpResponse
     * @param msg
     */
    private static void handlerError(HttpServletResponse httpResponse,String msg) {
        httpResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        String ERROR_PAGE = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>调用失败了</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>"+msg+"</h1>\n" +
                "</body>\n" +
                "</html>\n";
        try {
            httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
            httpResponse.getWriter().write(ERROR_PAGE);
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 直接请求支付码
     * @param request
     * @param response
     */
//    @GetMapping("/getPaymentQR")
//    @ResponseBody
//    public String getPaymentQR(HttpServletResponse httpResponse) {
//        QRCodeUtil qrCodeUtil = new QRCodeUtil();
//        String qrCode;
//        try {
//            qrCode = qrCodeUtil.createQRCode(ALPAY_QR_ADDR, 400, 400);
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }
//        httpResponse.setContentType("text/html;charset=" + CHARSET);
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("<!DOCTYPE html>\n" +
//                "<html lang=\"en\">\n" +
//                "<head>\n" +
//                "    <meta charset=\"UTF-8\">\n" +
//                "    <title>Title</title>\n" +
//                "</head>\n" +
//                "<body>\n" +
//                "<img src=\"").append(qrCode).append("\"></img>\n" +
//                "</body>\n" +
//                "</html>");
//        try {
//            httpResponse.getWriter().write(stringBuilder.toString());//直接将完整的表单html输出到页面
//            httpResponse.getWriter().flush();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }
//        return qrCode;
//    }

    @PostMapping("/paymentResult")
    @ResponseBody
    public void paymentResult(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramsMap=new HashMap<>();
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        //订单ID字符串
        String out_trade_no="null";
        //获取参数
        for (Map.Entry<String, String[]> paramEntry : requestParameterMap.entrySet()) {
            String key = paramEntry.getKey();
            String[] values = paramEntry.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                stringBuilder.append((i == values.length - 1) ? values[i] : values[i] + ",");
            }
            paramsMap.put(key, stringBuilder.toString());
        }

        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, ALIPAY_PUBLIC_KEY, CHARSET, AlipayConfig.SIGNTYPE);
        } catch (AlipayApiException e) {
            log.error("支付宝支付回调校验错误:",e);
        }
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //商户订单号
            out_trade_no = paramsMap.get("out_trade_no");
            //支付宝交易号
            String trade_no = paramsMap.get("trade_no");
            //交易状态
            String trade_status = paramsMap.get("trade_status");
            //交易完成时间
            String notify_time = paramsMap.get("notify_time");
            //total_amount
            String total_amount = paramsMap.get("total_amount");
            //更新订单状态
            if("TRADE_SUCCESS".equals(trade_status)){
                orderRecordService.update()
                        .set("status", OrderStatusEnum.SUCCESS)
                        .set("outPayNo",trade_no)
                        .set("paySuccessTime",notify_time)
                        .eq("id",out_trade_no).update();
                //查询订单信息
                OrderRecord orderRecord = orderRecordService.getById(out_trade_no);
                //新增用户的积分数
                userService.updateAllocationCredits(orderRecord.getUserId(),orderRecord.getTotalNum());
//                userInterfaceInfoService.updateAllocationInvokeNum(orderRecord.getInterfaceId(),
//                        orderRecord.getUserId(),orderRecord.getTotalNum());
                //支付成功
                try {
                    response.getWriter().print("success");
                    response.getWriter().close();
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "在返回给支付宝的消息时出现错误,订单ID:"+out_trade_no);
                }
            }else {
                //支付失败
                orderRecordService.update().set("status", OrderStatusEnum.FAILURE).eq("id",out_trade_no).update();
                log.warn("订单{}，处理错误，订单未支付",out_trade_no);
                try {
                    response.getWriter().print("failure");
                    response.getWriter().close();
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "在返回给支付宝的消息时出现错误,订单ID:"+out_trade_no);
                }
            }
        } else {
        // TODO 验签失败则记录异常日志，并在response中返回failure.
            log.error("订单{}，处理错误，订单验证失败",out_trade_no);
            try {
                response.getWriter().print("failure");
                response.getWriter().close();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "在返回给支付宝的消息时出现错误,订单ID:"+out_trade_no);
            }
        }
    }
    @GetMapping("/toPaymentSucceedPage")
    public String  toPaymentSucceedPage(){
        return "paymentSucceed";
    }
}
