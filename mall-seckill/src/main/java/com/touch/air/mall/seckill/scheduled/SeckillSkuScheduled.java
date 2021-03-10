package com.touch.air.mall.seckill.scheduled;

import com.touch.air.mall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 *  每天晚上3点：上架最近三天需要被秒杀的商品
 *  当天：00:00:00 - 23:59:59
 *  明天：00:00:00 - 23:59:59
 *  后天：00:00:00 - 23:59:59
 *
 * @author: bin.wang
 * @date: 2021/3/6 13:54
 */
@Service
public class SeckillSkuScheduled {
    @Resource
    private SeckillService seckillService;
    @Resource
    private RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    /**
     * TODO 幂等性处理
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1、重复上架就无须处理
        //分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }
}
