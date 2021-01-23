package com.touch.air.mall.third.controller;

import com.touch.air.common.utils.R;
import com.touch.air.mall.third.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: bin.wang
 * @date: 2021/1/22 14:34
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Resource
    private SmsComponent smsComponent;

    /**
     * 提供给其他服务调用的
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendCode(phone, code);
        return R.ok();
    }

}
