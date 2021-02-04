package com.touch.air.mall.cart.service;

import com.touch.air.mall.cart.vo.Cart;
import com.touch.air.mall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @author: bin.wang
 * @date: 2021/2/2 13:21
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);
}
