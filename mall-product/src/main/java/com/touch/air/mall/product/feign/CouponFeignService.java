package com.touch.air.mall.product.feign;

import com.touch.air.common.to.SkuReductionTO;
import com.touch.air.common.to.SpuBoundTO;
import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: bin.wang
 * @date: 2021/1/1 11:44
 */
@FeignClient(value = "touch-air-mall-coupon")
public interface CouponFeignService {

    /**
     * 1、 CouponFeignService.saveSpuBounds(spuBoundTO)
     *  1.1、@RequestBody 将这个对象转为json
     *  1.2、找到coupon服务，给 /coupon/spubounds/save 发送请求
     *       将上一步转的json 放在请求体位置，发送请求
     *  1.3、对方服务收到请求，请求体里有json数据
     *       @RequestBody SpuBoundsEntity spuBounds：将请求体中的json 转为 SpuBoundsEntity 这个类型
     * 2、只有json 数据模型是兼容的，双方服务无需使用同一个to
     *
     * @param spuBoundTO
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTO spuBoundTO);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}
