package com.touch.air.mall.cart.vo;

import cn.hutool.core.collection.CollUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写它的get方法，保证每次获取属性都会进行计算
 *
 * @author: bin.wang
 * @date: 2021/2/2 11:16
 */
public class Cart {
    /**
     * 商品数量
     */
    private Integer count;
    /**
     * 商品种类 数量
     */
    private Integer countType;
    List<CartItem> items;
    /**
     * 商品总价
     */
    private BigDecimal totalAmount;
    /**
     * 优惠价格
     */
    private BigDecimal reduce=new BigDecimal("0");



    public Integer getCount() {
        int allCount = 0;
        if (CollUtil.isNotEmpty(items)) {
            for (CartItem item : items) {
                allCount += item.getCount();
            }
        }
        return allCount;
    }

    public Integer getCountType() {
        int allCountType = 0;
        if (CollUtil.isNotEmpty(items)) {
            for (CartItem item : items) {
                allCountType += 1;
            }
        }
        return allCountType;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal allAmount = new BigDecimal("0");
        //1、计算购物项总价
        if (CollUtil.isNotEmpty(items)) {
            for (CartItem item : items) {
                allAmount = allAmount.add(item.getTotalPrice());
            }
        }
        //2、减除优惠
        allAmount = allAmount.subtract(reduce);
        return allAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
