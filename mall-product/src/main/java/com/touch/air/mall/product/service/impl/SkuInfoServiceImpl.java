package com.touch.air.mall.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.utils.R;
import com.touch.air.mall.product.dao.SkuInfoDao;
import com.touch.air.mall.product.entity.SkuImagesEntity;
import com.touch.air.mall.product.entity.SkuInfoEntity;
import com.touch.air.mall.product.entity.SpuInfoDescEntity;
import com.touch.air.mall.product.feign.SeckillFeignService;
import com.touch.air.mall.product.service.*;
import com.touch.air.mall.product.vo.SeckillInfoVo;
import com.touch.air.mall.product.vo.SkuItemSaleAttrVo;
import com.touch.air.mall.product.vo.SkuItemVo;
import com.touch.air.mall.product.vo.SpuItemGroupAttrVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private SeckillFeignService seckillFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String paramsKey = (String) params.get("key");
        if (StrUtil.isNotEmpty(paramsKey)) {
            wrapper.and(queryWrapper -> {
                queryWrapper.eq("id", paramsKey).or().like("spu_name", paramsKey);
            });
        }
        String catalogId = (String) params.get("catalogId");
        if (StrUtil.isNotEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (StrUtil.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (StrUtil.isNotEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");

        if (StrUtil.isNotEmpty(max)) {
            if ((new BigDecimal(max).compareTo(new BigDecimal(0)) == 1)) {
                wrapper.le("price", max);
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> spu_id = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return spu_id;
    }

    /**
     * 获取商品详情
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo item(Long skuId) {

        SkuItemVo skuItemVo = new SkuItemVo();
        //异步编排
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku基本信息获取 pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setSkuInfoEntity(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(info -> {
            //3、获取spu的销售属性
            List<SkuItemSaleAttrVo> saleAttrVoList = skuSaleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
            skuItemVo.setSaleAttrVoList(saleAttrVoList);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(info -> {
            //4、获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(info.getSpuId());
            skuItemVo.setSpuInfoDescEntity(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> groupAttrFuture = infoFuture.thenAcceptAsync(info -> {
            //5、获取spu的规则参数信息
            List<SpuItemGroupAttrVo> spuItemGroupAttrVoList = attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
            skuItemVo.setGroupAttrVoList(spuItemGroupAttrVoList);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2、sku的图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntityList = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setSkuImagesEntityList(skuImagesEntityList);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //3、查询当前sku是否参与秒优惠
            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckillInfo.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = skuSeckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfoVo(seckillInfoVo);
            }
        }, executor);
        //等待所有任务都完成，才可以返回
        try {
            CompletableFuture.allOf(saleAttrFuture, descFuture, groupAttrFuture, imageFuture,seckillFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return skuItemVo;
    }


}