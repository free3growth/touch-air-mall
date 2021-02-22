package com.touch.air.mall.order.feign;

import com.touch.air.mall.order.vo.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/2/19 14:55
 */
@FeignClient("touch-air-mall-ware")
public interface WmsFeignService {

    /**
     * 检查库存
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    List<SkuStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);
}
