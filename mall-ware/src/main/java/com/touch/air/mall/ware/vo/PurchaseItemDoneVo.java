package com.touch.air.mall.ware.vo;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/1/2 11:54
 */
@Data
public class PurchaseItemDoneVo {
    /**
     * 采购项Id
     */
    private Long itemId;
    private Integer status;
    private String reason;
}
