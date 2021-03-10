package com.touch.air.mall.seckill.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.touch.air.common.to.mq.SeckillOrderTo;
import com.touch.air.common.utils.R;
import com.touch.air.common.vo.MemberRespVo;
import com.touch.air.mall.seckill.feign.CouponFeignService;
import com.touch.air.mall.seckill.feign.ProductFeignService;
import com.touch.air.mall.seckill.interceptor.LoginUserInterceptor;
import com.touch.air.mall.seckill.service.SeckillService;
import com.touch.air.mall.seckill.to.SecKillSkuRedisTo;
import com.touch.air.mall.seckill.vo.SecKillSessionsWithSkuVo;
import com.touch.air.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: bin.wang
 * @date: 2021/3/6 14:01
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";
    private final String USER_SESSION_SKU_PREFIX = "seckill:user:session:user:";


    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            //上架商品
            List<SecKillSessionsWithSkuVo> data = r.getData(new TypeReference<List<SecKillSessionsWithSkuVo>>() {
            });
            log.info("最近三天的活动：" + data);
            //缓存到redis
            //1、缓存活动信息
            saveSessionInfos(data);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(data);

        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次
        long now = System.currentTimeMillis();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (CollUtil.isNotEmpty(keys)) {
            for (String key : keys) {
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long startTime = Long.parseLong(s[0]);
                long endTime = Long.parseLong(s[1]);
                if (now >= startTime && now <= endTime) {
                    //2、获取这个秒杀场次需要的所有商品信息
                    List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = ops.multiGet(range);
                    if (CollUtil.isNotEmpty(list)) {
                        List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                            SecKillSkuRedisTo redisTo = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                            //注意：如果当前秒杀已经开始，才可以返回随机码，不然不能返回
                            //redisTo.setRandomCode(null);
                            return redisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (CollUtil.isNotEmpty(keys)) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                //1_28
                boolean matches = Pattern.matches(regx, key);
                if (matches) {
                    String redisVal = ops.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(redisVal, SecKillSkuRedisTo.class);
                    //随机码是否需要返回
                    long now = System.currentTimeMillis();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (now >= startTime && now <= endTime) {
                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    /**
     * TODO 1、上架秒杀商品的时候，每一个数据都需要设置过期时间
     *      2、后续，收货、物流等简化
     */
    @Override
    public String seckill(String seckillId, String key, Integer num) {
        long seckillStart = System.currentTimeMillis();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = ops.get(seckillId);
        if (StrUtil.isNotEmpty(json)) {
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //2、校验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long now = System.currentTimeMillis();
            //2.1、校验时间合法性
            if (now >= startTime && now <= endTime) {
                //秒杀进行中
                //2.2、校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String id = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId().toString();
                if (randomCode.equals(key) && seckillId.equals(id)) {
                    //2.3、验证抢购数量是否合理
                    if (num <= redisTo.getSeckillLimit().intValue()) {
                        //2.4、该用户是否已经购买过；幂等性，如果秒杀成功就去占位 userId_sessionId_skuId
                        //SETNX
                        String redisKey = memberRespVo.getId() + "_" + id;
                        //当前场次结束，自动过期
                        long ttl = endTime - now;
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(USER_SESSION_SKU_PREFIX + redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占位成功，说明未购买过
                            //3、分布式信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean tryAcquire = semaphore.tryAcquire(num);
                            if (tryAcquire) {
                                //秒杀成功
                                //4、快速下单，发送MQ消息
                                String orderSn = UUID.randomUUID().toString().replace("_", "").substring(0, 15);
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setMemberId(memberRespVo.getId());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setOrderSn(orderSn);
                                seckillOrderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                seckillOrderTo.setSkuId(redisTo.getSkuId());
                                seckillOrderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                long seckillEnd = System.currentTimeMillis();
                                long time = seckillEnd - seckillStart;
                                log.info("秒杀耗时：" + time + "毫秒");
                                return orderSn;
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        return null;
    }

    private void saveSessionSkuInfos(List<SecKillSessionsWithSkuVo> data) {
        data.stream().forEach(secKillSessionsWithSkuVo -> {
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            secKillSessionsWithSkuVo.getRelationEntities().forEach(skuVO -> {
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!ops.hasKey(skuVO.getPromotionSessionId().toString() + "_" + skuVO.getSkuId().toString())) {
                    //准备hash操作
                    //缓存商品
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    //1、sku的基本数据
                    R r = productFeignService.skuInfo(skuVO.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        secKillSkuRedisTo.setSkuInfoVo(skuInfo);
                    }
                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(skuVO, secKillSkuRedisTo);
                    //3、设置上当前商品的秒杀时间信息
                    secKillSkuRedisTo.setStartTime(secKillSessionsWithSkuVo.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(secKillSessionsWithSkuVo.getEndTime().getTime());
                    //4、商品的随机码
                    secKillSkuRedisTo.setRandomCode(token);
                    String secKillSkuRedisToStr = JSON.toJSONString(secKillSkuRedisTo);
                    //如果当前商品的库存信息已经上架，就无需再次上架
                    //5、设置秒杀商品的分布式信号量作为库存扣减信息
                    //引入分布式信号量  目的：限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(skuVO.getSeckillCount().intValue());

                    ops.put(skuVO.getPromotionSessionId().toString() + "_" + skuVO.getSkuId().toString(), secKillSkuRedisToStr);
                }
            });
        });
    }

    private void saveSessionInfos(List<SecKillSessionsWithSkuVo> data) {
        data.stream().forEach(secKillSessionsWithSkuVo -> {
            Long startTime = secKillSessionsWithSkuVo.getStartTime().getTime();
            Long endTime = secKillSessionsWithSkuVo.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean aBoolean = stringRedisTemplate.hasKey(key);
            if (!aBoolean) {
                List<String> collect = secKillSessionsWithSkuVo.getRelationEntities().stream().map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }
}
