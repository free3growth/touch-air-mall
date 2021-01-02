package com.touch.air.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/2 10:19
 */
@Data
public class MergeVo {
    /**
     * 整单id
     */
    private Long purchaseId;
    /**
     * 合并项集合
     */
    private List<Long> items;
}
