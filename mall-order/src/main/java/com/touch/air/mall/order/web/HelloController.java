package com.touch.air.mall.order.web;

import com.touch.air.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author: bin.wang
 * @date: 2021/2/8 15:20
 */
@Controller
public class HelloController {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }

    @GetMapping("/test/createOrder")
    @ResponseBody
    public String CreateOrderTest() {
        OrderEntity orderEntity = new OrderEntity();
        //订单下单成功...
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());
        //给MQ发送消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order",orderEntity);
        return "ok";
    }
}
