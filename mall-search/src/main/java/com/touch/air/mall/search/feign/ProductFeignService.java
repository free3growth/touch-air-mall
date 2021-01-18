package com.touch.air.mall.search.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/17 16:27
 */
@FeignClient("touch-air-mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);


    @GetMapping("/product/brand/infos")
    public R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
