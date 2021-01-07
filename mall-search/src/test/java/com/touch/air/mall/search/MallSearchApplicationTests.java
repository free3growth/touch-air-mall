package com.touch.air.mall.search;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.touch.air.mall.search.config.MallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: bin.wang
 * @date: 2021/1/5 10:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void test() {
        System.out.println(restHighLevelClient);
    }

    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    public void testSave() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("username", "ZhSan", "age", 18, "gender","男");
        User user = new User();
        user.setUserName("ZhSan");
        user.setGender("男");
        user.setAge(18);
        String jsonStr = JSONUtil.toJsonStr(user);
        indexRequest.source(jsonStr, XContentType.JSON);//要保存的内容
        //执行操作
        IndexResponse index = restHighLevelClient.index(indexRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    /**
     * 复杂检索
     */
    @Test
    public void searchData() throws IOException {
        //1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //2、指定索引
        searchRequest.indices("bank");
        //3、指定DSL,检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //3.1、构造检索条件
        //searchSourceBuilder.query();
        //searchSourceBuilder.from();
        //searchSourceBuilder.size();
        //searchSourceBuilder.aggregation();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        //聚合操作
        //按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        //计算平均薪资
        AvgAggregationBuilder balanceAvgAgg = AggregationBuilders.avg("balanceAvgAgg").field("balance");
        searchSourceBuilder.aggregation(balanceAvgAgg);

        System.out.println("检索条件："+searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        //4、执行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        //5、分析结果
        System.out.println(searchResponse.toString());
        //5.1 获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit searchHit : hitsHits) {
            /**
             * "_index" : "bank",
             * "_type" : "accout",
             * "_id" : "1",
             * "_score" : 1.0,
             * "_source" : {}
             */
            String sourceAsString = searchHit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }
        //5.2 获取这次检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println("当前聚合："+aggregation.getName());
//        }
        Terms ageAggRes = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAggRes.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString+"===>"+bucket.getDocCount());
        }

        Avg balanceAvgAggRes = aggregations.get("balanceAvgAgg");
        System.out.println("平均薪资：" + balanceAvgAggRes.getValue());

    }


    /**
     * bank 账户信息
     */
    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }


    @Data
    class User{
        private String userName;
        private Integer age;
        private String gender;
    }

}
