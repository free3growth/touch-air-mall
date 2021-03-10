package com.touch.air.mall.seckill.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: bin.wang
 * @date: 2021/3/6 14:05
 */
@FeignClient("touch-air-mall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaySession")
    public R getLatest3DaySession();
}
