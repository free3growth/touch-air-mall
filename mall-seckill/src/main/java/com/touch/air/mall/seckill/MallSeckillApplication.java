package com.touch.air.mall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author: bin.wang
 * @date: 2021/3/6 11:12
 */
@SpringBootApplication
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
public class MallSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallSeckillApplication.class, args);
    }
}
