package com.touch.air.mall.ware.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: bin.wang
 * @date: 2021/1/2 12:29
 */
@FeignClient("touch-air-mall-product")
public interface ProductFeignService {

    /**
     * feign 请求的两种写法
     *  1. 让所有请求过网关
     *   1.1 @FeignClient("touch-air-mall-gateway"):给网关服务发请求
     *   1.2 /api/product/skuinfo/info/{skuId}
     *
     *  2. 直接指定具体某个微服务处理
     *   2.1 @FeignClient("touch-air-mall-product")：给商品服务发请求
     *   2.2 /product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
