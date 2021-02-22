package com.touch.air.mall.order.config;

import cn.hutool.core.util.ObjectUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: bin.wang
 * @date: 2021/2/19 09:44
 */
@Slf4j
@Configuration
public class MallFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1、RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                if (ObjectUtil.isNotNull(requestAttributes)) {
                    log.info("请求拦截器线程..." + Thread.currentThread() + "，线程id：" + Thread.currentThread().getId());
                    HttpServletRequest request = requestAttributes.getRequest();
                    //2、同步请求头数据，Cookie  request:老请求；requestTemplate:新请求
                    requestTemplate.header("Cookie", request.getHeader("Cookie"));
                }
            }
        };
    }
}
