package com.touch.air.mall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: bin.wang
 * @date: 2021/3/2 08:43
 */
@Slf4j
@Configuration
public class MyMqConfig {
    //@Bean Binding Queue Exchange
    /**
     * 容器中的 Binding、Queue、Exchange都会自动创建（RabbitMQ中没有的话）
     * RabbitMQ只要有，@Bean声明属性发生变化也不会覆盖
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue",true,false, false,arguments);
    }
    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",true,false, false);
    }
    @Bean
    public Exchange orderEventChange(){
        return new TopicExchange("order-event-exchange", true, false);
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    /**
     * 秒杀成功后，下单队列
     * @return
     */
    @Bean
    public Queue orderSeckillOrderQueue(){
        return new Queue("order.seckill.order.queue",true,false, false);
    }
    @Bean
    public Binding orderSeckillOrderBinding(){
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
//        log.info("收到过期的订单信息，准备关闭订单：" + entity.getOrderSn());
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//    }

}
