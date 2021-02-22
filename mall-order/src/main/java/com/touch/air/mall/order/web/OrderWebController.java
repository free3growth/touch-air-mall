package com.touch.air.mall.order.web;

import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.vo.OrderConfirmVo;
import com.touch.air.mall.order.vo.OrderSubmitVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author: bin.wang
 * @date: 2021/2/18 14:21
 */
@Controller
public class OrderWebController {

    @Resource
    private OrderService orderService;

    /**
     * 订单确认页
     *
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String ToTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     *
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo) {
        //下单：创建订单、验证令牌、验价格、锁库存
        //下单成功，跳转支付选择页
        //下单失败 回到订单确认页重新确认订单信息
        System.out.println("订单提交的数据：" + orderSubmitVo);
        return null;
    }
}
