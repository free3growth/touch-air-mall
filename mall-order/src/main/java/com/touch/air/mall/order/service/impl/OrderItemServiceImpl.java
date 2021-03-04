package com.touch.air.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.order.dao.OrderItemDao;
import com.touch.air.mall.order.entity.OrderEntity;
import com.touch.air.mall.order.entity.OrderItemEntity;
import com.touch.air.mall.order.entity.OrderReturnReasonEntity;
import com.touch.air.mall.order.service.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service("orderItemService")
//@RabbitListener(queues = {"mall-order-1"})
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws IOException {
        /**
         *  deliveryTag：消息标签，按顺序自增的
         *  multiple：是否批量
         */
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (deliveryTag % 2 == 0) {
            //确认
            channel.basicAck(deliveryTag,false);
            log.info("消息内容" + content.toString());
            log.info("确认了："+deliveryTag);
        }else{
            //否定确认
            channel.basicNack(deliveryTag, false, true);
            log.info("否定了："+deliveryTag);
        }

    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity orderEntity) {
        log.info("消息内容" + orderEntity);
    }

}