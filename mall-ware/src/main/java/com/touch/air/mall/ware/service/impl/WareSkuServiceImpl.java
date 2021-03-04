package com.touch.air.mall.ware.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.to.mq.OrderTo;
import com.touch.air.common.to.mq.StockDetailTo;
import com.touch.air.common.to.mq.StockLockedTo;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.utils.R;
import com.touch.air.mall.ware.dao.WareSkuDao;
import com.touch.air.mall.ware.entity.WareOrderTaskDetailEntity;
import com.touch.air.mall.ware.entity.WareOrderTaskEntity;
import com.touch.air.mall.ware.entity.WareSkuEntity;
import com.touch.air.mall.ware.exception.NoStockException;
import com.touch.air.mall.ware.feign.OrderFeignService;
import com.touch.air.mall.ware.feign.ProductFeignService;
import com.touch.air.mall.ware.service.WareOrderTaskDetailService;
import com.touch.air.mall.ware.service.WareOrderTaskService;
import com.touch.air.mall.ware.service.WareSkuService;
import com.touch.air.mall.ware.vo.OrderItemVo;
import com.touch.air.mall.ware.vo.OrderVo;
import com.touch.air.mall.ware.vo.SkuHasStockVo;
import com.touch.air.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Resource
    private WareOrderTaskService wareOrderTaskService;
    @Resource
    private OrderFeignService orderFeignService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {
        //解锁
        //1、查询数据库关于这个订单的锁定库存信息
        //1.1、有结果：锁定成功，但业务调用失败
        //  1.1.1 没有这个订单，必须解锁
        //  1.1.2 有这个订单，根据订单状态：已取消：解锁库存；没取消：不能解锁
        //1.2、无结果：库存锁定失败，这种情况无需解锁
        StockDetailTo stockDetailTo = stockLockedTo.getStockDetailTo();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(stockDetailTo.getId());
        if (ObjectUtil.isNotNull(detailEntity)) {
            //解锁
            Long orderTaskId = stockLockedTo.getOrderTaskId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(orderTaskId);
            if (ObjectUtil.isNotNull(taskEntity)) {
                String orderSn = taskEntity.getOrderSn();
                //根据订单号查询订单当前状态
                R r = orderFeignService.getOrderStatus(orderSn);
                if (r.getCode() == 0) {
                    //订单数据返回成功
                    OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                    });
                    if (ObjectUtil.isNull(orderVo) || orderVo.getStatus() == 4) {
                        //订单不存在，原因：库存锁定成功，订单后续业务异常，订单回滚了,必须解锁
                        //订单已取消，解锁库存
                        if (detailEntity.getLockStatus() == 1) {
                            //库存详情单中，库存状态为已锁定才可以解锁
                            this.baseMapper.unLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum());
                            //更新库存工作单的状态为已解锁
                            WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                            taskDetailEntity.setId(detailEntity.getId());
                            //已解锁
                            taskDetailEntity.setLockStatus(2);
                            wareOrderTaskDetailService.updateById(taskDetailEntity);
                        }
                    }
                } else {
                    //消息拒绝，然后重新会队列，等待下一个消费者来消费
                    //统一异常捕捉，消息重新退回队列
                    throw new RuntimeException("远程服务，获取订单信息失败");
                }
            }
        } else {
            //无需解锁
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(OrderTo orderTo) {
        //防止订单服务卡顿，导致订单状态一直不变为关单；
        //库存消息先到期，查询订单状态仍为新建状态，什么都不做就消费了
        //导致卡顿的订单，永远没法解锁库存
        String orderSn = orderTo.getOrderSn();
        //查询最新的库存状态，防止重复解锁库存
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        //根据库存工作单id，找到多有已锁定的库存，进行解锁
        List<WareOrderTaskDetailEntity> orderTaskDetailEntities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", wareOrderTaskEntity.getId())
                        .eq("lock_status", 1)
        );
        if (CollUtil.isNotEmpty(orderTaskDetailEntities)) {
            for (WareOrderTaskDetailEntity orderTaskDetailEntity : orderTaskDetailEntities) {
                this.baseMapper.unLockStock(orderTaskDetailEntity.getSkuId(), orderTaskDetailEntity.getWareId(), orderTaskDetailEntity.getSkuNum());
                //更新库存工作单的状态为已解锁
                WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                taskDetailEntity.setId(orderTaskDetailEntity.getId());
                //已解锁
                taskDetailEntity.setLockStatus(2);
                wareOrderTaskDetailService.updateById(taskDetailEntity);
            }
        }

    }

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
        } else {
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
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
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
     *
     * @param wareSkuLockVo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
        /**
         * 保存库存工作单的详情
         * 方便追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

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
                //1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
                //2、锁定失败，全部回滚
                for (Long aLong : wareIds) {
                    //锁定成功返回1，否则返回0
                    Long count = this.baseMapper.lockSkuStock(skuId, aLong, num);
                    if (count == 0) {
                        //当前仓库锁定失败，重试下一个仓库
                    } else {
                        //锁定成功，直接下一个商品
                        skuStocked = true;
                        //TODO 告诉MQ库存锁定成功,触发自动解锁逻辑
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", num, wareOrderTaskEntity.getId(), aLong, 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        stockLockedTo.setOrderTaskId(wareOrderTaskEntity.getId());
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                        //只存detailId不行，防止回滚以后找不到数据
                        stockLockedTo.setStockDetailTo(stockDetailTo);
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                        break;
                    }
                }
                if (skuStocked == false) {
                    //for循环走完了，当前商品所有仓库都没有库存
                    throw new NoStockException(skuId);
                }
            } else {
                //当前商品没有库存 ，下单失败
                throw new NoStockException(skuId);
            }
            allLock = true;
        }
        return allLock;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}