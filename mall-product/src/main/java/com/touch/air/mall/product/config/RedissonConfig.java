package com.touch.air.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author: bin.wang
 * @date: 2021/1/13 10:53
 */
@Configuration
public class RedissonConfig {

    /**
     * 所有对redisson的使用 都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson(@Value("${spring.redis.host}") String redisUrl) throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redisUrl+":6379");
        //、根据Config 创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
