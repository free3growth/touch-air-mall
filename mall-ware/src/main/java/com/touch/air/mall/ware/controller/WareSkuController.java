package com.touch.air.mall.ware.controller;

import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.R;
import com.touch.air.mall.ware.entity.WareSkuEntity;
import com.touch.air.mall.ware.service.WareSkuService;
import com.touch.air.mall.ware.vo.SkuHasStockVo;
import com.touch.air.mall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 14:31:57
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/order/lock")
    public R orderLock(@RequestBody WareSkuLockVo wareSkuLockVo) {
//        List<LockStockResultVo> lockStockResultVos = wareSkuService.orderLockStock(wareSkuLockVo);
        try {
            wareSkuService.orderLockStock(wareSkuLockVo);
            return R.ok();
        }catch (Exception e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/hasstock")
    public List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds) {
        //sku_id,stock
        List<SkuHasStockVo> skuHasStockVos = wareSkuService.getSkusHasStock(skuIds);
        return skuHasStockVos;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
