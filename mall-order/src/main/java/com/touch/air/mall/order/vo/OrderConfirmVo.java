package com.touch.air.mall.order.vo;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要用到的数据
 *
 * @author: bin.wang
 * @date: 2021/2/18 15:00
 */
@ToString
public class OrderConfirmVo {

    /**
     * 收货地址列表
     */
    @Getter
    @Setter
    private List<MemberAddressVo> addressVos;

    /**
     * 所有选择的购物项
     */
    @Getter
    @Setter
    private List<OrderItemVo> itemVos;

    //发票记录

    /**
     * 优惠券（积分）信息
     */
    @Getter
    @Setter
    private Integer integration;

    /**
     * 防重令牌
     */
    @Getter
    @Setter
    private String orderToken;

    /**
     * 是否有库存
     */
    @Getter
    @Setter
    private Map<Long, Boolean> stocks;

    /**
     * 订单总额
     */
    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if (CollUtil.isNotEmpty(itemVos)) {
            for (OrderItemVo itemVo : itemVos) {
                total = total.add(itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount().toString())));
            }
        }
        return total;
    }

    /**
     * 总数量
     */
    public Integer getCount() {
        Integer count = 0;
        if (CollUtil.isNotEmpty(itemVos)) {
            for (OrderItemVo itemVo : itemVos) {
                count += itemVo.getCount();
            }
        }
        return count;
    }

    /**
     * 应付价格
     */
    public BigDecimal getPayPrice() {
        BigDecimal payPrice = new BigDecimal("0");
        if (CollUtil.isNotEmpty(itemVos)) {
            for (OrderItemVo itemVo : itemVos) {
                payPrice = payPrice.add(itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount().toString())));
            }
        }
        return payPrice;
    }
}
