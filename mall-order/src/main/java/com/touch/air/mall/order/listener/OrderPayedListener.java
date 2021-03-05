package com.touch.air.mall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.touch.air.mall.order.config.AlipayTemplate;
import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: bin.wang
 * @date: 2021/3/5 14:36
 */
@RestController
@Slf4j
public class OrderPayedListener {
    @Resource
    private OrderService orderService;
    @Resource
    private AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo payAsyncVo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //只要收到了支付的异步通知，订单支付成功；我们就返回success，支付宝就不会再通知了
        log.info("支付宝通知到位了,通知内容：" + payAsyncVo.getTrade_status());
        //核心操作：验签（防止恶意攻击）
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type());
        /**
         * 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
         * 	2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
         * 	3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
         * 	4、验证app_id是否为该商户本身。
         */
        if (signVerified) {
            //验证成功
            log.info("签名验证成功...");
            String result = orderService.handlePayResult(payAsyncVo);
            return result;
        } else {
            //验证失败
            log.info("签名验证失败...");
            return "error";
        }
    }

}
