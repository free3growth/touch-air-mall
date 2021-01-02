package com.touch.air.mall.ware.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.constant.WareConstant;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.ware.dao.PurchaseDao;
import com.touch.air.mall.ware.entity.PurchaseDetailEntity;
import com.touch.air.mall.ware.entity.PurchaseEntity;
import com.touch.air.mall.ware.service.PurchaseDetailService;
import com.touch.air.mall.ware.service.PurchaseService;
import com.touch.air.mall.ware.service.WareSkuService;
import com.touch.air.mall.ware.vo.MergeVo;
import com.touch.air.mall.ware.vo.PurchaseDoneVo;
import com.touch.air.mall.ware.vo.PurchaseItemDoneVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceive(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode()).or().eq( "status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (ObjectUtil.isNull(purchaseId)) {
            //新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //TODO 确认采购单状态是0或1才可以

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.saveOrUpdateBatch(purchaseDetailEntities);
    }

    /**
     * 采购单的id
     * @param ids
     */
    @Override
    public void received(List<Long> ids) {
        //1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item->{
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }else {
                return false;
            }
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //2. 改变采购单的状态
        this.updateBatchById(purchaseEntities);
        //3. 改变采购项的状态
        purchaseEntities.forEach(purchaseEntity -> {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(purchaseEntity.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.saveOrUpdateBatch(collect);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        //1.改变采购单状态
        Long id = purchaseDoneVo.getId();
        //2.改变采购项状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo purchaseItemDoneVo : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseItemDoneVo.getStatus() == WareConstant.PurchaseDetailStatusEnum.ERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(purchaseItemDoneVo.getStatus());
            }else{
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将成功采购的进行入库
                PurchaseDetailEntity purchaseDetailServiceById = purchaseDetailService.getById(purchaseItemDoneVo.getItemId());
                wareSkuService.addStock(purchaseDetailServiceById.getSkuId(), purchaseDetailServiceById.getWareId(), purchaseDetailServiceById.getSkuNum());
            }
            //
            purchaseDetailEntity.setId(purchaseItemDoneVo.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.ERROR.getCode());
        this.updateById(purchaseEntity);
    }

}