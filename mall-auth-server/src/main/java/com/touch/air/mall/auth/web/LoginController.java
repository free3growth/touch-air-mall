package com.touch.air.mall.auth.web;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.touch.air.common.constant.AuthServerConstant;
import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.utils.R;
import com.touch.air.mall.auth.feign.MemberFeignService;
import com.touch.air.mall.auth.feign.ThirdPartFeignService;
import com.touch.air.mall.auth.vo.UserLoginVo;
import com.touch.air.mall.auth.vo.UserRegisterVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: bin.wang
 * @date: 2021/1/22 09:58
 */
@Controller
public class LoginController {

    @Resource
    private ThirdPartFeignService thirdPartFeignService;

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一个请求直接跳转到一个页面,避免写很多空方法
     * <p>
     * SpringMVC viewController:将请求和页面映射进来
     * com.touch.air.mall.auth.config.WebConfig
     *
     * @return
     */
    //@GetMapping({"/", "/login.html"})
    //public String loginPage() {
    //   return "login";
    //}
    //@GetMapping({ "/register.html"})
    //public String registerPage() {
    //    return "register";
    //}

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        // 1、接口防刷
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StrUtil.isNotEmpty(redisCode)) {
            long preTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - preTime < 60 * 1000) {
                //60s内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //2、验证码校验 redis临时存储 存的时候，key-phone value-code  sms:code:15862969594 --->12138
        String uuid = UUID.randomUUID().toString().substring(0, 5);
        String postCode = uuid + "_" + System.currentTimeMillis();
        //redis缓存验证码，15分钟过期  防止同一个手机号在60s内再次发送验证码(加上系统时间)
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, postCode, 15, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, uuid);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //参数校验出错，转发到注册页
            //RedirectAttributes 模拟重定向携带数据
            //TODO 模拟session，但这种方式在分布式下会出现问题  分布式下的session问题
            // TODO 重定向携带数据，利用session原理，将数据存放在session中，只要刷新或者跳转，取出这个数据之后，就会删除session里的数据
            redirectAttributes.addFlashAttribute("errors", errors);
            //Request method 'POST' not supported
            //用户注册--->/register[post]--->转发/register.html(路径映射默认都是get方式访问的)
            //携带数据，必须完整路径
            return "redirect:http://auth.mall.com/register.html";
        }else {
            //校验验证码
            String postCode = userRegisterVo.getCode();
            String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
            if (StrUtil.isNotEmpty(redisCode)) {
                if (postCode.equals(redisCode.split("_")[0])) {
                    //删除验证码；令牌机制
                    stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
                    //验证码正确
                    //注册，调用远程服务
                    R r = memberFeignService.register(userRegisterVo);
                    if (r.getCode() == 0) {
                        //成功
                        //注册成功回到登录页面
                        return "redirect:http://auth.mall.com/login.html";
                    } else {
                        //失败
                        Map<String, String> errors = new HashMap<>();
                        errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                        redirectAttributes.addFlashAttribute("errors", errors);
                        return "redirect:http://auth.mall.com/register.html";
                    }
                } else {
                    //验证码未通过
                    Map<String, String> errors = new HashMap<>();
                    errors.put("code", "验证码错误");
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/register.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码过期");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.mall.com/register.html";
            }
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo,RedirectAttributes redirectAttributes) {
        //表单提交，不需要@RequestBody
        R r = memberFeignService.login(userLoginVo);
        if (r.getCode() == 0) {
            return "redirect:http://mall.com/";
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }

    }

}
