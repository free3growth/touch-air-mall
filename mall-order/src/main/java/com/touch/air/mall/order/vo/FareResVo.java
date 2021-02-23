package com.touch.air.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: bin.wang
 * @date: 2021/2/22 09:13
 */
@Data
public class FareResVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
