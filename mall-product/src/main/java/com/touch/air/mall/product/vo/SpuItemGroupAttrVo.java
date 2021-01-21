package com.touch.air.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/20 17:04
 */
@Data
@ToString
public class SpuItemGroupAttrVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
