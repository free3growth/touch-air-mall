package com.touch.air.mall.order.feign;

import com.touch.air.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/2/18 16:54
 */
@FeignClient("touch-air-mall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}
