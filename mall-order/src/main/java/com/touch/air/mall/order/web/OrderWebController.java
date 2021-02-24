package com.touch.air.mall.order.web;

import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.vo.OrderConfirmVo;
import com.touch.air.mall.order.vo.OrderSubmitVo;
import com.touch.air.mall.order.vo.SubmitOrderResVo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model) {
        SubmitOrderResVo submitOrderResVo = orderService.submitOrder(orderSubmitVo);
        log.info("下单返回结果集："+submitOrderResVo.toString());
        if (submitOrderResVo.getCode() == 0) {
            //下单成功来到支付选择页
            model.addAttribute("submitOrderRes", submitOrderResVo);
            return "pay";
        }else{
            String msg = "下单失败";
            //下单失败，回到确认页重新确认订单消息
            switch (submitOrderResVo.getCode()) {
                case 1:
                    msg += "：订单信息过期，请刷新后再次提交";
                    break;
                case 2:
                    msg += "：订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "：商品库存不足";
                    break;
            }
            log.info(msg);
            return "redirect:http://order.mall.com/toTrade";
        }
    }
}
