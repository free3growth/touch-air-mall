package com.touch.air.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/20 17:06
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValue;
}
