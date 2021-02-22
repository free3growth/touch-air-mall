package com.touch.air.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 *
 * @author: bin.wang
 * @date: 2021/2/22 13:27
 */
@Data
public class OrderSubmitVo {
    /**
     * 收货地址id
     */
    private Long addrId;
    /**
     * 支付方式
     */
    private Integer payType;

    //无需提交需要购买的商品，去购物车载获取一次
    //优惠、发票等等 暂不处理

    /**
     * 防重令牌
     */
    private String orderToken;
    /**
     * 应付金额  验价
     */
    private BigDecimal payPrice;
    //用户相关信息，直接session中获取
    /**
     * 订单备注
     */
    private String note;
}
