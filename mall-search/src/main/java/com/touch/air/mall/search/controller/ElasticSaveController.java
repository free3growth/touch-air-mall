package com.touch.air.mall.search.controller;

import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.to.es.SkuEsModel;
import com.touch.air.common.utils.R;
import com.touch.air.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/6 14:18
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {
    @Resource
    private ProductSaveService productSaveService;
    /**
     *  上架商品
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean up = false;
        try {
             up= productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误：{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (up) {
            return R.ok();
        }else{
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
