package com.touch.air.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.touch.air.common.to.mq.SeckillOrderTo;
import com.touch.air.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: bin.wang
 * @date: 2021/3/9 14:34
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Component
@Slf4j
public class OrderSeckillListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        log.info("准备创建秒杀的详细信息:" + seckillOrderTo);
        try {
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
