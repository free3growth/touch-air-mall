package com.touch.air.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.product.dao.SkuSaleAttrValueDao;
import com.touch.air.mall.product.entity.SkuSaleAttrValueEntity;
import com.touch.air.mall.product.service.SkuSaleAttrValueService;
import com.touch.air.mall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        SkuSaleAttrValueDao saleAttrValueDao = this.baseMapper;
        List<SkuItemSaleAttrVo> skuItemSaleAttrVoList = saleAttrValueDao.getSaleAttrsBySpuId(spuId);
        return skuItemSaleAttrVoList;
    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {

        return baseMapper.getSaleAttrsValueAsString(skuId);
    }

}