package com.touch.air.mall.product.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: bin.wang
 * @date: 2021/3/8 08:45
 */
@FeignClient("touch-air-mall-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable(value = "skuId") Long skuId);
}
