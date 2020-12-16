package com.touch.air.mall.third;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: bin.wang
 * @date: 2020/12/16 16:03
 */
@EnableDiscoveryClient
@SpringBootApplication
public class MallThirdPartApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallThirdPartApplication.class, args);
    }
}
