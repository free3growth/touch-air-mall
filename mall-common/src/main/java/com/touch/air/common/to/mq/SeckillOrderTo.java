package com.touch.air.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: bin.wang
 * @date: 2021/3/9 11:09
 */
@Data
public class SeckillOrderTo {
    private String orderSn;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    private Integer num;
    private Long memberId;

}
