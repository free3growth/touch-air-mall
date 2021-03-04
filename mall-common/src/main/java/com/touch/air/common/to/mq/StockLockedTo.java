package com.touch.air.common.to.mq;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2021/3/2 17:21
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单的id
     */
    private Long orderTaskId;

    private StockDetailTo stockDetailTo;


}
