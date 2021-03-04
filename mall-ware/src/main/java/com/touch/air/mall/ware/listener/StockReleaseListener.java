package com.touch.air.mall.ware.listener;

import com.rabbitmq.client.Channel;
import com.touch.air.common.to.mq.OrderTo;
import com.touch.air.common.to.mq.StockLockedTo;
import com.touch.air.mall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: bin.wang
 * @date: 2021/3/3 09:35
 */
@Service
@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Resource
    private WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     * 1、下单成功，库存锁定成功，接下来的业务调用失败，导致回滚，之前锁定的库存就要自动解锁
     * 2、订单失败，锁库存失败
     * <p>
     * 注意：一旦库存解锁失败，消息一定要重新返回队列重试解锁--MQ启用收到ACK模式
     *
     * @param stockLockedTo
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存的消息:" + stockLockedTo);
        try {
            wareSkuService.unLockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
    @RabbitHandler
    public void handleOrderClose(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("收到订单关单的消息,准备解锁库存:" + orderTo);
        try {
            wareSkuService.unLockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
