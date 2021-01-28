package com.touch.air.mall.auth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.touch.air.common.constant.AuthServerConstant;
import com.touch.air.common.utils.HttpUtils;
import com.touch.air.common.utils.R;
import com.touch.air.common.vo.MemberRespVo;
import com.touch.air.mall.auth.feign.MemberFeignService;
import com.touch.air.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交请求
 *
 * @author: bin.wang
 * @date: 2021/1/23 14:56
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Resource
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession httpSession, HttpServletResponse httpServletResponse) throws IOException {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        //1、根据code 获取 access token
        log.info("code:" + code);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "xxxx");
        paramMap.put("client_secret", "xxxx");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://auth.mall.com/oauth2.0/weibo/success");
        paramMap.put("code", code);
        HttpResponse response = null;
        try {
             response= HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //1.1 处理响应结果
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取access token
            String resJson = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(resJson, SocialUser.class);

            //1.2 当前用户如果是第一次登录，就自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            //登录或者注册用户 远程方法
            R r = memberFeignService.oauthWeiboLogin(socialUser);
            if (r.getCode() == 0) {
                //2、登录成功就跳回首页
                MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功，用户信息：" + memberRespVo);
                //1、第一次使用session,命令浏览器保存 jsessionId这个cookie
                // 以后浏览器访问哪个网站就会带上这个网站的cookie
                //子域之间：mall.com、auth.mall.com order.mall.com
                // (指定域名为父域名),即使是子域名系统的，也能被父域直接使用
                httpSession.setAttribute(AuthServerConstant.LOGIN_USER,memberRespVo);
                //TODO 1、默认发的令牌 session=xxxx,作用域：当前域 也就是认证服务，商品服务要想获取，必须修改作用域 auth.mall.com 为父域 mall.com
                //TODO 2、使用json系列化方式 来序列号对象数据到redis中
                //httpServletResponse.addCookie(new Cookie("JSESSIONID", "123").setDomain(""));
                return "redirect:http://mall.com";
            }else{
                return "redirect:http://mall.com/login.html";
            }
        }else{
            return "redirect:http://mall.com/login.html";
        }
    }

}
