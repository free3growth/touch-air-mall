package com.touch.air.mall.product.vo;

import com.touch.air.mall.product.entity.SkuImagesEntity;
import com.touch.air.mall.product.entity.SkuInfoEntity;
import com.touch.air.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/20 15:45
 */
@Data
public class SkuItemVo {
    /**
     * sku基本信息获取 pms_sku_info
     */
    private SkuInfoEntity skuInfoEntity;

    /**
     * sku的图片信息 pms_sku_images
     */
    private List<SkuImagesEntity> skuImagesEntityList;

    /**
     * 获取spu的销售属性
     */
    private List<SkuItemSaleAttrVo> saleAttrVoList;

    /**
     * 获取spu的介绍
     */
    private SpuInfoDescEntity spuInfoDescEntity;

    /**
     * 获取spu的规则参数信息
     */
    private List<SpuItemGroupAttrVo> groupAttrVoList;

    /**
     * 是否有货
     * 默认有货
     */
    private boolean hasStock = true;


}
