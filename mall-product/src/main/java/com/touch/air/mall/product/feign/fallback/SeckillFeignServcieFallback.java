package com.touch.air.mall.product.feign.fallback;

import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.utils.R;
import com.touch.air.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: bin.wang
 * @date: 2021/3/12 09:22
 */
@Slf4j
@Component
public class SeckillFeignServcieFallback implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法调用...");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
