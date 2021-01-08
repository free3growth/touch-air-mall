package com.touch.air.mall.product.app;

import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.R;
import com.touch.air.common.validator.group.AddGroup;
import com.touch.air.common.validator.group.UpdateGroup;
import com.touch.air.common.validator.group.UpdateStatusGroup;
import com.touch.air.mall.product.entity.BrandEntity;
import com.touch.air.mall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*, BindingResult bindingResult*/) {
//        if (bindingResult.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            //1.获取校验的错误结果
//            bindingResult.getFieldErrors().forEach((item)->{
//                //2.获取错误提示
//                String message = item.getDefaultMessage();
//                //3.获取错误的属性名字
//                String field = item.getField();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        }else{
        brandService.save(brand);

        return R.ok();
//        }
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand) {
//        brandService.updateById(brand);
        brandService.updateDetail(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/updateStatus")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
