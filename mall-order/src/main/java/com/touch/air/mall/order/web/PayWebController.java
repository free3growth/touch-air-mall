package com.touch.air.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.touch.air.mall.order.config.AlipayTemplate;
import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author: bin.wang
 * @date: 2021/3/4 14:48
 */
@Controller
@Slf4j
public class PayWebController {
    @Resource
    private AlipayTemplate alipayTemplate;
    @Resource
    private OrderService orderService;

    /**
     * 1、将支付页让浏览器展示
     * 2、支付成功以后，跳转到用户订单列表页 --修改 AlipayTemplate 中的同步通知
     *
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String  payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        //支付宝返回的是一个页面，将此页面直接交给浏览器就行
        log.info("支付返回结果：" + pay);
        return pay;
    }
}
