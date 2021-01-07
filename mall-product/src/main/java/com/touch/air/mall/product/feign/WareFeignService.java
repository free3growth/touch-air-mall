package com.touch.air.mall.product.feign;

import com.touch.air.mall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 库存系统远程调用
 *
 * @author: bin.wang
 * @date: 2021/1/6 13:41
 */
@FeignClient(value = "touch-air-mall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    public List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);
}
