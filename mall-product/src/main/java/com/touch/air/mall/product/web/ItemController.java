package com.touch.air.mall.product.web;

import com.touch.air.mall.product.service.SkuInfoService;
import com.touch.air.mall.product.vo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * 商品详情
 *
 * @author: bin.wang
 * @date: 2021/1/20 14:34
 */
@Controller
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable(value = "skuId") Long skuId, Model model) {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
