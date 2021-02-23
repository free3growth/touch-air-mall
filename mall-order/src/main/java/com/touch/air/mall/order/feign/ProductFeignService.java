package com.touch.air.mall.order.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: bin.wang
 * @date: 2021/2/23 11:33
 */
@FeignClient("touch-air-mall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/getSpuInfoBySkuId/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
