package com.touch.air.mall.member.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: bin.wang
 * @date: 2020/12/7 08:58
 */
@FeignClient(value = "touch-air-mall-coupon")
public interface CouponsFeignService {

    /**
     * 远程调用 优惠券服务
     * @return
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();

}
