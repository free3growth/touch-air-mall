package com.touch.air.mall.ware;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
@Slf4j
@SpringBootTest
class MallWareApplicationTests {
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
        DirectExchange directExchange = new DirectExchange("123456",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("路由交换机[{}]创建成功", directExchange.getName());
    }

    @Test
    void contextLoads() {
    }

}
