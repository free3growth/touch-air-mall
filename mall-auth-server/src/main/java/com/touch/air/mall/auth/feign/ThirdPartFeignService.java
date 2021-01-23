package com.touch.air.mall.auth.feign;

import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: bin.wang
 * @date: 2021/1/22 14:42
 */
@FeignClient("touch-air-mall-third-part")
public interface ThirdPartFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
