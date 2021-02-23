package com.touch.air.mall.order.to;

import com.touch.air.mall.order.entity.OrderEntity;
import com.touch.air.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/2/23 09:09
 */
@Data
public class OrderCreateTo {

    private OrderEntity orderEntity;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
