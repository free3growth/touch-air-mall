package com.touch.air.mall.member.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: bin.wang
 * @date: 2021/3/5 08:45
 */
@FeignClient("touch-air-mall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);

    @RequestMapping("/order/order/list")
    R list(@RequestParam Map<String, Object> params);
}
