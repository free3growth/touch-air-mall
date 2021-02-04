package com.touch.air.mall.cart.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/2/3 09:58
 */
@FeignClient("touch-air-mall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/strList/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable(value = "skuId") Long skuId);
}
