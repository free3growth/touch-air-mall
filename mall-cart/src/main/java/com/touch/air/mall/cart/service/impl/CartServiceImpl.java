package com.touch.air.mall.cart.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.touch.air.common.utils.R;
import com.touch.air.mall.cart.feign.ProductFeignService;
import com.touch.air.mall.cart.interceptor.CartInterceptor;
import com.touch.air.mall.cart.service.CartService;
import com.touch.air.mall.cart.vo.Cart;
import com.touch.air.mall.cart.vo.CartItem;
import com.touch.air.mall.cart.vo.SkuInfoVo;
import com.touch.air.mall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author: bin.wang
 * @date: 2021/2/2 13:20
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    private final String CART_PREFIX = "mall.cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StrUtil.isEmpty(res)) {
            CartItem cartItem = new CartItem();
            //购物车无此商品 添加
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //1、远程查询当前要添加的商品信息，并封装进cartItem
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfoVo.getPrice());
            }, threadPoolExecutor);

            CompletableFuture<Void> getAttrValuesTask = CompletableFuture.runAsync(() -> {
                //2、远程查询sku的组合信息
                //远程方法多了，串联进行，耗费时间 1+2+3秒...  使用异步编排 可以有效节省时间 3秒的完成了 1秒 2秒的也都完成了，最终只需要3秒
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, threadPoolExecutor);
            CompletableFuture.allOf(getAttrValuesTask, getSkuInfoTask).get();
            //3、商品添加到购物车
            String cartItemStr = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemStr);
            return cartItem;
        }else{
            //有此商品 修改数量即可
            CartItem item = JSON.parseObject(res, CartItem.class);
            item.setCount(item.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(item));
            return item;

        }
    }

    /**
     * 获取购物车中 某个购物项
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (ObjectUtil.isNotNull(userInfoTo.getUserId())) {
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //2、如果临时购物车的数据还没有进行合并
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (CollUtil.isNotEmpty(tempCartItems)) {
                //临时购物车有数据，需要合并
                tempCartItems.stream().forEach(cartItem -> {
                    try {
                        addToCart(cartItem.getSkuId(), cartItem.getCount());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //3、获取登录后的购物车数据【包含合并的临时购物车数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else{
            //2、没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }


    /**
     * 获取到 我们要操作的购物车
     * @return
     */
    private BoundHashOperations getCartOps() {
        //1、只要是同一次请求，在任何位置都能得到数据
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //2、判断是登录用户还是临时用户
        String cartKey = "";
        if (ObjectUtil.isNotNull(userInfoTo.getUserId())) {
            //用户已登录
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else{
            //临时用户
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        return boundHashOperations;
    }

    private List<CartItem> getCartItems(String key) {
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(key);
        List<Object> values = boundHashOperations.values();
        if (CollUtil.isNotEmpty(values)) {
            List<CartItem> cartItemList = values.stream().map(val -> {
                String str = (String) val;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItemList;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer check) {
        //根据当前登录状态，获取绑定的购物车
        BoundHashOperations cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        //修改完的数据，存入redis
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        //根据当前登录状态，获取绑定的购物车
        BoundHashOperations cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        //修改完的数据，存入redis
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    @Override
    public void deleteItem(Long skuId) {
        //根据当前登录状态，获取绑定的购物车
        BoundHashOperations cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
