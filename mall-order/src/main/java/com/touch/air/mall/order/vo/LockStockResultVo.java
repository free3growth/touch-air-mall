package com.touch.air.mall.order.vo;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/2/23 14:25
 */
@Data
public class LockStockResultVo {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
