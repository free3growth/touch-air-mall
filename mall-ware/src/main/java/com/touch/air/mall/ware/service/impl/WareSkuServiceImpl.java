package com.touch.air.mall.ware.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.utils.R;
import com.touch.air.mall.ware.dao.WareSkuDao;
import com.touch.air.mall.ware.entity.WareSkuEntity;
import com.touch.air.mall.ware.exception.NoStockException;
import com.touch.air.mall.ware.feign.ProductFeignService;
import com.touch.air.mall.ware.service.WareSkuService;
import com.touch.air.mall.ware.vo.OrderItemVo;
import com.touch.air.mall.ware.vo.SkuHasStockVo;
import com.touch.air.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        if (StrUtil.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1. 判断如果还没有这个库存记录
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (CollUtil.isNotEmpty(wareSkuEntities)) {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }else{
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //远程查询skuName,如果失败 无需回滚
              //1.自己catch异常
              //2.高级篇
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String)data.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.baseMapper.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(id -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前库存总量  库存-锁定的数量
            Long count = this.baseMapper.getSkuStock(id);
            skuHasStockVo.setSkuId(id);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单 锁定库存
     * @param wareSkuLockVo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
        //1、按照下单的收货地址，找到一个就近仓库，锁定库存
        //2、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            //1、查询当前商品在哪个仓库 有库存
            List<Long> wareIds = this.baseMapper.listWareIdHashStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        //2、锁定库存
        Boolean allLock = false;
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {
            Boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            Integer num = skuWareHasStock.getNum();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (CollUtil.isNotEmpty(wareIds)) {
                for (Long aLong : wareIds) {
                    //锁定成功返回1，否则返回0
                    Long count = this.baseMapper.lockSkuStock(skuId, aLong, num);
                    if (count == 0) {
                        //当前仓库锁定失败，重试下一个仓库
                    }else{
                        //锁定成功，直接下一个商品
                        skuStocked = true;
                        break;
                    }
                }
                if (skuStocked == false) {
                    //for循环走完了，当前商品所有仓库都没有库存
                    throw new NoStockException(skuId);
                }
            }else{
                //当前商品没有库存 ，下单失败
                throw new NoStockException(skuId);
            }
            allLock = true;
        }
        return allLock;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}