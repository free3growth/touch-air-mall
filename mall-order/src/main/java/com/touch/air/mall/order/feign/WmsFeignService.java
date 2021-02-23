package com.touch.air.mall.order.feign;

import com.touch.air.common.utils.R;
import com.touch.air.mall.order.vo.SkuStockVo;
import com.touch.air.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * 模拟 运费计算
     * @param addrId
     * @return
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 锁定库存
     *
     * @param wareSkuLockVo
     * @return
     */
    @PostMapping("/ware/waresku/order/lock")
    R orderLock(@RequestBody WareSkuLockVo wareSkuLockVo);
}
