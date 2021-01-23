package com.touch.air.mall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: bin.wang
 * @date: 2021/1/22 13:16
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     *  视图映射
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /**
         *      @GetMapping({"/", "/login.html"})
         *          public String loginPage()
         *          return "login"
         */
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/register.html").setViewName("register");

    }
}
