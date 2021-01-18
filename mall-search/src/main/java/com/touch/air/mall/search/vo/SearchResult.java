package com.touch.air.mall.search.vo;

import com.touch.air.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
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

    /**
     * 导航页码
     */
    private List<Integer> pageNavs;

    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 已存在的筛选属性
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

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
