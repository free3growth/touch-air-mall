package com.touch.air.mall.order.controller;

import com.touch.air.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author: bin.wang
 * @date: 2021/2/8 11:31
 */
@RestController
public class RabbitController {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send")
    public String sendMessage() {
        for (int i = 0; i < 10; i++) {
//            if (i % 2 == 0) {
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setName("其他七天理由");
                orderReturnReasonEntity.setCreateTime(new Date());
                //发送对象,序列化机制，将对象写出去，对象必须实现序列化
                rabbitTemplate.convertAndSend("direct-exchange", "order-1", orderReturnReasonEntity, new CorrelationData(UUID.randomUUID().toString()));
//            }else{
//                OrderEntity orderEntity = new OrderEntity();
//                orderEntity.setOrderSn(UUID.randomUUID().toString());
//                rabbitTemplate.convertAndSend("direct-exchange", "order-1", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
//            }
        }
        return "ok";
    }
}
