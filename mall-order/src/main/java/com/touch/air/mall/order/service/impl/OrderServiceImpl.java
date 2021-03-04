package com.touch.air.mall.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.to.mq.OrderTo;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.utils.R;
import com.touch.air.common.vo.MemberRespVo;
import com.touch.air.mall.order.constant.OrderConstant;
import com.touch.air.mall.order.constant.OrderStatusEnum;
import com.touch.air.mall.order.dao.OrderDao;
import com.touch.air.mall.order.entity.OrderEntity;
import com.touch.air.mall.order.entity.OrderItemEntity;
import com.touch.air.mall.order.feign.CartFeignService;
import com.touch.air.mall.order.feign.MemberFeignService;
import com.touch.air.mall.order.feign.ProductFeignService;
import com.touch.air.mall.order.feign.WmsFeignService;
import com.touch.air.mall.order.interceptor.LoginUserInterceptor;
import com.touch.air.mall.order.service.OrderItemService;
import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.to.OrderCreateTo;
import com.touch.air.mall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    public static ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private WmsFeignService wmsFeignService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //当前登录的用户信息
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        log.info("主线程..." + Thread.currentThread() + "，线程id：" + Thread.currentThread().getId());
        //取出一开始进来的上下文，每个异步线程都重新set
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            log.info("address线程..." + Thread.currentThread() + "，线程id：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1、远程查询所有的收货地址列表
            List<MemberAddressVo> memberAddressVos = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddressVos(memberAddressVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            log.info("cart线程..." + Thread.currentThread() + "，线程id：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2、远程查询购物车所有选中的购物项
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItemVos(currentUserCartItems);
        }, threadPoolExecutor).thenRunAsync(() -> {
            List<OrderItemVo> itemVos = confirmVo.getItemVos();
            List<Long> collect = itemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuStockVo> skuStock = wmsFeignService.getSkusHasStock(collect);
            if (CollUtil.isNotEmpty(skuStock)) {
                Map<Long, Boolean> map = skuStock.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::isHasStock));
                confirmVo.setStocks(map);
            }
        }, threadPoolExecutor);
        //3、查询用户积分
        confirmVo.setIntegration(memberRespVo.getIntegration());
        //4、总价、应付价格自动计算  OrderConfirmVo
        //5、防重令牌（幂等性）
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(cartFuture, addressFuture).get();
        log.info("confirm:" + confirmVo);
        return confirmVo;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单的最新状态（已付款...）
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单，只有订单是代付款的状态
            OrderEntity order = new OrderEntity();
            order.setId(entity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            //关单成功，主动通知解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

//    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResVo submitOrderResVo = new SubmitOrderResVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        submitOrderResVo.setCode(0);
        //1、验证令牌【令牌的对比和删除必须保证原子性】
        //lua脚本 - 保证原子性   返回 0：删除失败   1：删除成功
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (execute == 0L) {
            //令牌验证失败
            submitOrderResVo.setCode(1);
            return submitOrderResVo;
        } else {
            //1、令牌验证成功（原子性）
            //2、创建订单
            submitVoThreadLocal.set(orderSubmitVo);
            OrderCreateTo orderCreateTo = createOrder();
            //验价
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            BigDecimal payAmount = orderCreateTo.getOrderEntity().getPayAmount();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比成功
                //3、保存订单数据到数据库
                saveOrder(orderCreateTo);
                //4、锁定库存，只有有异常回滚订单数据
                //订单号、订单项（skuId、数量、skuName）
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(orderCreateTo.getOrderEntity().getOrderSn());
                List<OrderItemVo> orderItemVos = orderCreateTo.getOrderItems().stream().map(entity -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(entity.getSkuId());
                    itemVo.setCount(entity.getSkuQuantity());
                    return itemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVos);
                //远程锁定库存
                //存在事务问题：库存成功了，但是网络原因超时了，订单回滚，但是库存扣减了
                //为了保证高并发，库存服务自己回滚（消息队列）：1、可以发消息给库存服务 2、库存服务自动解锁
                R orderLock = wmsFeignService.orderLock(wareSkuLockVo);
                if (orderLock.getCode() == 0) {
                    //锁定成功
                    submitOrderResVo.setOrderEntity(orderCreateTo.getOrderEntity());
                    //TODO 远程扣减积分
                    //模拟异常，全局事务回滚
                    //int i = 10 / 0;
                    //TODO 订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderCreateTo.getOrderEntity());
                    return submitOrderResVo;
                } else {
                    //锁定失败
                    submitOrderResVo.setCode(3);
                    return submitOrderResVo;
                }
            } else {
                //对比失败
                submitOrderResVo.setCode(2);
                return submitOrderResVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    /**
     * 保存订单信息
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity orderEntity = orderCreateTo.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);
        orderCreateTo.setOrderEntity(orderEntity);
        //2、订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        orderCreateTo.setOrderItems(orderItemEntities);
        //3、验价   计算价格相关
        computePrice(orderEntity, orderItemEntities);
        return orderCreateTo;
    }


    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        //会员信息
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        entity.setMemberId(memberRespVo.getId());

        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareResVo fareResVo = fare.getData(new TypeReference<FareResVo>() {
        });
        //运费
        entity.setFreightAmount(fareResVo.getFare());
        //收货人信息
        entity.setReceiverCity(fareResVo.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResVo.getAddress().getDetailAddress());
        entity.setReceiverName(fareResVo.getAddress().getName());
        entity.setReceiverPhone(fareResVo.getAddress().getPhone());
        entity.setReceiverProvince(fareResVo.getAddress().getProvince());
        entity.setReceiverPostCode(fareResVo.getAddress().getPostCode());
        entity.setReceiverRegion(fareResVo.getAddress().getRegion());
        //设置订单的状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (CollUtil.isNotEmpty(currentUserCartItems)) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建每一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1、订单信息
        //2、商品的spu信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoVo spuInfoBySkuIdData = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoBySkuIdData.getId());
        orderItemEntity.setSpuBrand(spuInfoBySkuIdData.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoBySkuIdData.getSpuName());
        orderItemEntity.setCategoryId(spuInfoBySkuIdData.getCatalogId());
        //3、商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";"));
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息（忽略）
        //5、积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        //6、订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        //当前订单项的实际金额
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal real = origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(real);
        return orderItemEntity;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        if (CollUtil.isNotEmpty(orderItemEntities)) {
            BigDecimal total = new BigDecimal("0.0");
            BigDecimal couponAmount = new BigDecimal("0.0");
            BigDecimal integrationAmount = new BigDecimal("0.0");
            BigDecimal promotionAmount = new BigDecimal("0.0");
            BigDecimal giftGrowth = new BigDecimal("0.0");
            BigDecimal giftIntegration = new BigDecimal("0.0");
            //订单的总额，叠加每一个订单项的总额信息
            for (OrderItemEntity entity : orderItemEntities) {
                total = total.add(entity.getRealAmount());
                couponAmount = couponAmount.add(entity.getCouponAmount());
                integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
                promotionAmount = promotionAmount.add(entity.getPromotionAmount());
                //积分、成长值
                giftGrowth = giftGrowth.add(new BigDecimal(entity.getGiftGrowth().toString()));
                giftIntegration = giftIntegration.add(new BigDecimal(entity.getGiftIntegration().toString()));
            }
            //总价
            orderEntity.setTotalAmount(total);
            //应付总额：总价＋运费
            orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
            orderEntity.setPromotionAmount(promotionAmount);
            orderEntity.setCouponAmount(couponAmount);
            orderEntity.setIntegrationAmount(integrationAmount);
            //积分、成长值
            orderEntity.setGrowth(giftGrowth.intValue());
            orderEntity.setIntegration(giftIntegration.intValue());
            //删除状态
            orderEntity.setDeleteStatus(0);

        }
    }

}