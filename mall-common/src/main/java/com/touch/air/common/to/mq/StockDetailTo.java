package com.touch.air.common.to.mq;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/3/2 17:31
 */
@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;
    /**
     * 锁定状态
     */
    private Integer lockStatus;
}
