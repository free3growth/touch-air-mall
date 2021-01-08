package com.touch.air.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类vo
 *
 * @author: bin.wang
 * @date: 2021/1/8 08:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {
    private String catalog1Id;
    /**
     * 三级子分类
     */
    private List<Catalog3Vo> catalog3List;
    private String name;
    private String id;

    /**
     * 三级分类vo
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class  Catalog3Vo{
        /**
         * 父分类，二级分类id
         */
        private String catalog2Id;
        private String name;
        private String id;
    }
}
