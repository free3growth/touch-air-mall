package com.touch.air.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/2/23 14:20
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks;
}
