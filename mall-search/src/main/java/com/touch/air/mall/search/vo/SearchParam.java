package com.touch.air.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递的查询条件
 *
 * @author: bin.wang
 * @date: 2021/1/15 13:15
 */
@Data
public class SearchParam {

    /**
     * 完整请求参数
     * catalog3Id=225&keyword=小米&sort=saleCount_desc&hasStock=0/1&skuPrice=1_500&brandId=1&attrs=1_android：iOS&attrs=2_5.寸：6.5寸
     */

    /**
     * 页面传递过来的全文比配关键字
     */
    private String keyword;
    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hostScore_asc/desc
     */
    private String sort;

    /**
     * 过滤条件
     * <p>
     * hasStock(是否有货)、skuPrice价格区间、brandId(品牌id 可以多选)、catalog3Id、attrs(属性)
     * <p>
     * hasStock=0（无库存）/1（有库存）
     * skuPrice=1_500/_500/500_(1到500、500以内的、大于500的)
     * brandId=1&brandId=2
     * attrs=1_android：iOS&attrs=2_5.寸：6.5寸
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    /**
     * 分页 页码
     */
    private Integer pageNum = 1;

    /**
     *原生的所有查询条件
     */
    private String _url;
}
