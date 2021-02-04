package com.touch.air.mall.cart.controller;

import com.touch.air.mall.cart.service.CartService;
import com.touch.air.mall.cart.vo.Cart;
import com.touch.air.mall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author: bin.wang
 * @date: 2021/2/2 13:29
 */
@Controller
@Slf4j
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 浏览器有一个cookie；user-key：标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器保存，以后的每次访问都会带上
     * <p>
     * 登录:session有
     * 未登录：按照cookie里带来的user-key来做
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     * --> 创建一个拦截器
     *
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    /**
     * 添加商品到购物车
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        log.info("skuId:" + skuId + "   num:" + num);
        CartItem cartItem = cartService.addToCart(skuId, num);
        //redirectAttributes.addAttribute():将数据放在session里面可以在页面取出，但是只能取一次
        //redirectAttributes.addFlashAttribute():数据自动拼接在url请求路径上
        redirectAttributes.addAttribute("skuId", skuId);
        //重定向，防止用户刷新url，就完成一次新增，新增与查看分离
        return "redirect:http://cart.mall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功页面，再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    /**
     * 购物项选中
     *
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.mall.com/cart.html";
    }

    /**
     * 购物项增减
     *
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);
        return "redirect:http://cart.mall.com/cart.html";
    }

    /**
     * 购物项删除
     *
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.com/cart.html";
    }
}
