package com.touch.air.mall.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.touch.air.common.to.es.SkuEsModel;
import com.touch.air.mall.search.config.MallElasticSearchConfig;
import com.touch.air.mall.search.constant.EsConstant;
import com.touch.air.mall.search.service.MallSearchService;
import com.touch.air.mall.search.vo.SearchParam;
import com.touch.air.mall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: bin.wang
 * @date: 2021/1/15 13:14
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    /**
     * 操作es
     */
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {
        //动态构建出查询需要的DSL语句
        SearchResult searchResult = null;
        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        //2、执行检索请求
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            //3、分析响应结果，封装成需要的格式
            searchResult = buildSearchResult(response,searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("页面所需数据："+JSON.toJSONString(searchResult));
        return searchResult;
    }

    /**
     * 构建检索请求
     * 模糊匹配、过滤（选择属性、分类、品牌、价格区间、库存）、排序、分页、高亮、聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //构建DSL语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1、查询：模糊匹配、过滤（选择属性、分类、品牌、价格区间、库存）
        // 构建bool-query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1、bool-must匹配
        if (StrUtil.isNotEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        //1.2、bool-filter过滤
        //  选择属性、分类、品牌、价格区间、库存
        //  bool-filter-按照三级分类id过滤
        if (ObjectUtil.isNotNull(searchParam.getCatalog3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        //  bool-filter-按照品牌id过滤
        if (CollUtil.isNotEmpty(searchParam.getBrandId())) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        //  bool-filter-按照是否有库存过滤 (页面是0/1 es中是true/false)
        if (ObjectUtil.isNotNull(searchParam.getHasStock())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        //  bool-filter-按照价格区间过滤
        if(StrUtil.isNotEmpty(searchParam.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            //单值
            String[] ranges = searchParam.getSkuPrice().split("_");
            if (searchParam.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(ranges[0]);
            }else if (searchParam.getSkuPrice().endsWith("_")) {
                rangeQuery.gte(ranges[0]);
            }else{
                //区间
                rangeQuery.gte(ranges[0]).lte(ranges[1]);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        //  bool-filter-按照所有指定的属性过滤
        if (CollUtil.isNotEmpty(searchParam.getAttrs())) {
            //attrs=1_android：iOS&attrs=2_5.寸：6.5寸
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] attrArr = attrStr.split("_");
                //检索的属性id
                String attrId = attrArr[0];
                //这个属性检索用的值
                String[] attrValueArr = attrArr[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValueArr));
                //每一个都要生成一个嵌入式nested的查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

        /**
         * 排序、分页、高亮
         */
        //2.1 排序
        if (StrUtil.isNotEmpty(searchParam.getSort())) {
            //排序条件
            //   sort=saleCount_asc/desc
            //   sort=skuPrice_asc/desc
            String[] sortArr= searchParam.getSort().split("_");
            SortOrder sortOrder = sortArr[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortArr[0], sortOrder);
        }

        //2.2 分页
        searchSourceBuilder.from((searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3 高亮
        if (StrUtil.isNotEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        /**
         * 聚合分析
         */
        //3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合(品牌名称、logo)
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        //3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        //分类聚合的子聚合(分类名称)
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        //3.3 属性聚合
        // nested 嵌套
        NestedAggregationBuilder nestedAttr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //属性的子聚合（属性名、属性值）
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        nestedAttr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(nestedAttr_agg);

        System.out.println("构建的DSL:" + searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }


    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();

        SearchHits responseHits = response.getHits();
        //命中的所有记录
        SearchHit[] hits = responseHits.getHits();

        //1、返回的所有查询到的商品 _source
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        if (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!hit.getHighlightFields().isEmpty()) {
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModelList);

        //2、当前所有商品所涉及到的所有属性 nestedAttr_agg
        Aggregations responseAggregations = response.getAggregations();
        // ParsedNested 嵌入式的
        ParsedNested attr_agg = responseAggregations.get("attr_agg");
        //属性id的聚合
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVoList = new ArrayList<>();
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //得到属性的id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            //属性名称的聚合
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //属性值的聚合
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> valueList = attr_value_agg.getBuckets().stream().map(val -> val.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(valueList);
            attrVoList.add(attrVo);
        }
        searchResult.setAttrVoList(attrVoList);

        //3、当前所有商品所涉及到的所有品牌信息 brand_agg
        ParsedLongTerms brand_agg = responseAggregations.get("brand_agg");
        List<? extends Terms.Bucket> brand_aggBuckets = brand_agg.getBuckets();
        List<SearchResult.BrandVo> brandVoList = new ArrayList<>();
        for (Terms.Bucket bucket : brand_aggBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //得到品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            //得到品牌名称
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            //得到品牌logo
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            brandVoList.add(brandVo);
        }
        searchResult.setBrandVoList(brandVoList);

        //4、当前所有商品所涉及到的所有分类信息 catalog_agg
        ParsedLongTerms catalog_agg = responseAggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalog_aggBuckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogVoList = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_aggBuckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            //得到分类名称
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            catalogVoList.add(catalogVo);
        }
        searchResult.setCatalogVoList(catalogVoList);
        //5、分页信息
        searchResult.setPageNum(searchParam.getPageNum());
        //总记录数
        long total = responseHits.getTotalHits().value;
        searchResult.setTotal(total);
        //计算得到：总数取余每页条数没有余数 值就是总页数，有余数，值加1为总页数
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        searchResult.setTotalPages(totalPages);
        return searchResult;
    }
}
