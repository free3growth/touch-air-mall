package com.touch.air.mall.cart.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.touch.air.common.constant.AuthServerConstant;
import com.touch.air.common.constant.CartServerConstant;
import com.touch.air.common.vo.MemberRespVo;
import com.touch.air.mall.cart.vo.UserInfoTo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标请求
 *
 * @author: bin.wang
 * @date: 2021/2/2 13:41
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo memberRespVo = null;
        if (ObjectUtil.isNotNull(session.getAttribute(AuthServerConstant.LOGIN_USER))) {
             memberRespVo= (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        }
        if (ObjectUtil.isNotNull(memberRespVo)) {
            //登录了
            userInfoTo.setUserId(memberRespVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //user-key
                String cookieName = cookie.getName();
                if (cookieName.equals(CartServerConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //如果没有临时用户，一定分配临时用户
        if (StrUtil.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //在目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.getTempUser()) {
            //不存在临时用户时，一定要分配一个
            Cookie cookie = new Cookie(CartServerConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //设置cookie作用域和过期时间
            cookie.setDomain("mall.com");
            cookie.setMaxAge(30*24*60*60);
            response.addCookie(cookie);
        }
    }
}
