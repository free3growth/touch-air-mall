package com.touch.air.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/2 11:53
 */
@Data
public class PurchaseDoneVo {
    /**
     * 采购单Id
     */
    private Long id;

    /**
     * 采购项列表
     */
    private List<PurchaseItemDoneVo> items;

}
