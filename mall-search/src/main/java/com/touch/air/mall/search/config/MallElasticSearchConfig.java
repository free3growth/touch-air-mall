package com.touch.air.mall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1.导入依赖
 * 2.编写配置，给容器中注入一个RestHighLevelClient
 * 参照API   https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.6/java-rest-high-getting-started-initialization.html
 *
 * @author: bin.wang
 * @date: 2021/1/5 09:32
 */
@Configuration
public class MallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient(@Value("${elasticsearch.host}") String esUrl) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esUrl, 9200, "http")));
//                        new HttpHost("192.168.83.133", 9200, "http")));
        return client;
    }
}
