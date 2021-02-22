package com.touch.air.mall.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.common.vo.MemberRespVo;
import com.touch.air.mall.order.constant.OrderConstant;
import com.touch.air.mall.order.dao.OrderDao;
import com.touch.air.mall.order.entity.OrderEntity;
import com.touch.air.mall.order.feign.CartFeignService;
import com.touch.air.mall.order.feign.MemberFeignService;
import com.touch.air.mall.order.feign.WmsFeignService;
import com.touch.air.mall.order.interceptor.LoginUserInterceptor;
import com.touch.air.mall.order.service.OrderService;
import com.touch.air.mall.order.vo.MemberAddressVo;
import com.touch.air.mall.order.vo.OrderConfirmVo;
import com.touch.air.mall.order.vo.OrderItemVo;
import com.touch.air.mall.order.vo.SkuStockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

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
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVo> itemVos = confirmVo.getItemVos();
            List<Long> collect = itemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuStockVo> skuStock = wmsFeignService.getSkusHasStock(collect);
            if (CollUtil.isNotEmpty(skuStock)) {
                Map<Long, Boolean> map = skuStock.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::isHasStock));
                confirmVo.setStocks(map);
            }
        },threadPoolExecutor);
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

}