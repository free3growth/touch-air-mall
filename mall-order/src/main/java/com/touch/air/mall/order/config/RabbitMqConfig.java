package com.touch.air.mall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author: bin.wang
 * @date: 2021/2/8 09:27
 */
@Slf4j
@Configuration
public class RabbitMqConfig {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitTemplate
     * @PostConstruct: RabbitMqConfig对象创建完成以后，执行这个方法
     *
     * 1、设置确认回调 -- 服务器收到消息
     * 2、消息正确抵达队列进行回调
     *
     */
    @PostConstruct
    public void initRabbitTemplate() {
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData   当前消息的唯一关联数据(这个是消息的唯一id)
             * @param ack   消息是否成功收到   只要消息抵达broker，ack=true
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirmCallback - CorrelationData:" + correlationData);
                log.info("confirmCallback - ack:" + ack);
                log.info("confirmCallback - cause:" + cause);
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 触发时机，消息没有投递给指定的队列，就触发这个失败回调
             * @param message   投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 恢复的文本内容
             * @param exchange  当时这个消息发给哪个交换机
             * @param routingKey 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("returnCallback - message:" + message);
                log.info("returnCallback - replyCode:" + replyCode);
                log.info("returnCallback - replyText:" + replyText);
                log.info("returnCallback - exchange:" + exchange);
                log.info("returnCallback - routingKey:" + routingKey);
            }
        });

    }

}
