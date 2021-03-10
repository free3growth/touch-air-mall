package com.touch.air.mall.seckill.service;

import com.touch.air.mall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/3/6 14:00
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String seckill(String seckillId, String key, Integer num);
}
