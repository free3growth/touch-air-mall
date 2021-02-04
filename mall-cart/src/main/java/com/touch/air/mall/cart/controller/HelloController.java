package com.touch.air.mall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: bin.wang
 * @date: 2021/2/2 09:51
 */
@Controller
public class HelloController {

    @GetMapping("/list")
    public String list(){
        return "cartList";
    }

    @GetMapping("/")
    public String success(){
        return "success";
    }
}
