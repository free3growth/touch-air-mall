package com.touch.air.mall.ssoclient.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/1/28 11:17
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    private String ssoServerUrl;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 无需登录就可访问
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")

    public String hello() {
        return "hello";
    }


    @GetMapping("/employees")
    public String employees(Model model, @RequestParam(value = "token", required = false) String token, HttpSession httpSession) {
        String s = "";
        if (StrUtil.isNotEmpty(token)) {
            //去ssoserver登录成功跳转回来就会带token
            //1、TODO 去ssoserver获取当前token真正对应的用户信息
            httpSession.setAttribute("loginUser", "z3");
        }
        Object user = httpSession.getAttribute("loginUser");
        if (ObjectUtil.isNotNull(user)) {
            //已经登录
            List<String> emps = new ArrayList<>();
            emps.add("z3");
            emps.add("l4");
            emps.add("w5");
            model.addAttribute("emps", emps);
            return "list";
        } else {
            //未登录
            //跳转到登录服务器，进行登录
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }
    }


}
