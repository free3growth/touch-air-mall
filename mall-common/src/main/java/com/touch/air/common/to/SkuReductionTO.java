package com.touch.air.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/1 12:11
 */
@Data
public class SkuReductionTO {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
