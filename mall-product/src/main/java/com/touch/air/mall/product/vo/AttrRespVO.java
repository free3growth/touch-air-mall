package com.touch.air.mall.product.vo;

import lombok.Data;

/**
 * @author: bin.wang
 * @date: 2020/12/28 14:46
 */
@Data
public class AttrRespVO extends AttrVO {
    /**
     * 所属分类名称
     */
    private String catelogName;
    /**
     * 所属分组名字
     */
    private String groupName;

    /**
     * 回显级联分类完整路径
     */
    private Long[] catelogPath;
}
