package com.touch.air.mall.seckill.controller;

import com.touch.air.common.utils.R;
import com.touch.air.mall.seckill.service.SeckillService;
import com.touch.air.mall.seckill.to.SecKillSkuRedisTo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: bin.wang
 * @date: 2021/3/7 13:15
 */
@Controller
public class SeckillController {
    @Resource
    private SeckillService seckillService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable(value = "skuId") Long skuId) {
        SecKillSkuRedisTo redisTo=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(redisTo);
    }

    @GetMapping("/seckill")
    public String seckill(
            @RequestParam("seckillId") String seckillId,
            @RequestParam("key") String key,
            @RequestParam("num") Integer num,
            Model model) {
        //1、先判断是否登录（拦截器）
        //2、获取当前秒杀商品的详细信息
        String orderSn = seckillService.seckill(seckillId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
