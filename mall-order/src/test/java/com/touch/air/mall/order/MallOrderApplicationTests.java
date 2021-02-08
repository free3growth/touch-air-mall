package com.touch.air.mall.order;

import com.touch.air.mall.order.entity.OrderEntity;
import com.touch.air.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author: bin.wang
 * @date: 2021/2/8 08:36
 */
@Slf4j
@SpringBootTest
public class MallOrderApplicationTests {
    @Resource
    private AmqpAdmin amqpAdmin;

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 1、如何创建Exchange、Queue、Binding
     *  1.1、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void test() {
        DirectExchange directExchange = new DirectExchange("direct-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("路由交换机[{}]创建成功", directExchange.getName());
    }

    /**
     * 创建队列
     */
    @Test
    public void createQueue() {
        Queue queue = new Queue("mall-order-1", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("队列[{}]创建成功", queue.getName());
    }
    /**
     * 创建绑定
     *  destination;【目的地】
     *  destinationType;【目的地类型】
     *  routingKey;【路由键】
     *  exchange 【交换机】
     *  arguments 【自定义参数】
     */
    @Test
    public void createBinding() {
        Binding binding = new Binding("mall-order-1", Binding.DestinationType.QUEUE, "direct-exchange", "order-1", new HashMap<>());
        amqpAdmin.declareBinding(binding);
        log.info("绑定[{}]创建成功", binding.getRoutingKey());
    }


    /**
     * 发送消息
     */
    @Test
    public void sendMessage() {
//        String str = "hello-mall-order-1";
//        rabbitTemplate.convertAndSend("direct-exchange", "order-1", str);
//        log.info("消息[{}]发送完成", str);

        //发送对象
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setName("其他七天理由");
                orderReturnReasonEntity.setCreateTime(new Date());
                //发送对象,序列化机制，将对象写出去，对象必须实现序列化
                rabbitTemplate.convertAndSend("direct-exchange", "order-1", orderReturnReasonEntity);
            }else{
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("direct-exchange", "order-1", orderEntity);
            }
        }
    }

}
