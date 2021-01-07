package com.touch.air.mall.product.feign;

import com.touch.air.common.to.es.SkuEsModel;
import com.touch.air.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/7 09:31
 */
@FeignClient(value = "touch-air-mall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
