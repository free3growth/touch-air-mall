package com.touch.air.mall.seckill.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.touch.air.common.constant.AuthServerConstant;
import com.touch.air.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: bin.wang
 * @date: 2021/2/18 14:34
 */
@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        boolean match = new AntPathMatcher().match("/seckill", request.getRequestURI());
        if (match ) {
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            log.info("秒杀服务共享的用户信息：" + attribute);
            if (ObjectUtil.isNotNull(attribute)) {
                //同一个线程 threadLocal 共享数据
                loginUser.set(attribute);
                return true;
            }else{
                //未登录,跳转登录
                request.getSession().setAttribute("msg", "请先进行登录");
                response.sendRedirect("http://auth.mall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
