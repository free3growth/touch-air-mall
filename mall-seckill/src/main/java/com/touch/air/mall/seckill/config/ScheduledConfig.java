package com.touch.air.mall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 开启异步任何和定时调度
 *
 * @author: bin.wang
 * @date: 2021/3/6 13:55
 */
@EnableAsync
@EnableScheduling
@Configuration
public class ScheduledConfig {

}
