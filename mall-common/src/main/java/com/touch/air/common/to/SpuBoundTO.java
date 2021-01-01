package com.touch.air.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: bin.wang
 * @date: 2021/1/1 11:51
 */
@Data
public class SpuBoundTO {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
