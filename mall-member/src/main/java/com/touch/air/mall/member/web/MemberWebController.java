package com.touch.air.mall.member.web;

import com.alibaba.fastjson.JSON;
import com.touch.air.common.utils.R;
import com.touch.air.mall.member.feign.OrderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: bin.wang
 * @date: 2021/3/4 17:08
 */
@Slf4j
@Controller
public class MemberWebController {
    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  Model model) {
        //查出当前登录的用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        log.info("远程查询订单结果集：" + JSON.toJSONString(r));
        if (r.getCode() == 0) {
            model.addAttribute("orders", r);
        }
        return "orderList";
    }
}
