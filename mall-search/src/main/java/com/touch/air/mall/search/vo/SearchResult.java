package com.touch.air.mall.search.vo;

import com.touch.air.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/15 13:53
 */
@Data
public class SearchResult {
    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 总页码
     */
    private Integer totalPages;
    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前查询到的结果 所有涉及到的品牌
     */
    private List<BrandVo> brandVoList;

    /**
     * 当前查询到的结果 所有涉及到的所有属性
     */
    private List<AttrVo> attrVoList;

    /**
     * 当前查询到的结果 所有涉及到的所有分类
     */
    private List<CatalogVo> catalogVoList;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

}
