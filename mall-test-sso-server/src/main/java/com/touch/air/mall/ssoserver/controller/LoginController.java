package com.touch.air.mall.ssoserver.controller;

import cn.hutool.core.util.StrUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author: bin.wang
 * @date: 2021/1/28 11:17
 */
@Controller
public class LoginController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model, @CookieValue(value = "sso_token", required = false) String sso_token) {
        if (StrUtil.isNotEmpty(sso_token)) {
            //说明之前有人登陆过，给浏览器留下了痕迹
            return "redirect:" + url + "?token=" + sso_token;
        } else {
            //如果没有，就展示登录
            model.addAttribute("url", url);
            return "login";
        }
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, String url, HttpServletResponse response) {
        if (StrUtil.isNotEmpty(username) && StrUtil.isNotEmpty(password)) {
            //登录成功，调回之前的页面
            //假设把登录成功的用户存起来
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);

            Cookie sso_token = new Cookie("sso_token", uuid);
            response.addCookie(sso_token);

            return "redirect:" + url + "?token=" + uuid;
        } else {
            //失败，展示登录页
            return "login";
        }

    }


}
