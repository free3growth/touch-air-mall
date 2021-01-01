package com.touch.air.mall.product.controller;

import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.R;
import com.touch.air.mall.product.entity.AttrEntity;
import com.touch.air.mall.product.entity.AttrGroupEntity;
import com.touch.air.mall.product.service.AttrAttrgroupRelationService;
import com.touch.air.mall.product.service.AttrGroupService;
import com.touch.air.mall.product.service.AttrService;
import com.touch.air.mall.product.service.CategoryService;
import com.touch.air.mall.product.vo.AttrGroupRelationVO;
import com.touch.air.mall.product.vo.AttrGroupWithAttrsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrgroupRelationService;


    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelog) {
        //1、查出当前分类下的所有属性分组
        //2. 查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVO> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelog);
        return R.ok().put("data", vos);
    }

    /**
     * 新增分组与属性的关联关系
     */
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody List<AttrGroupRelationVO> attrGroupRelationVOS){
        attrgroupRelationService.addBatch(attrGroupRelationVOS);
        return R.ok();
    }

    /**
     * 查询分组未关联的属性
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getNoRelationAttr(@RequestParam Map<String, Object> params,@PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page=attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 关联移除
     */
    @RequestMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVO[] attrGroupRelationVOS){
        attrGroupService.deleteRelation(attrGroupRelationVOS);

        return R.ok();
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable(value = "attrgroupId")Long attrgroupId){
        List<AttrEntity> attrEntities=attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntities);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catId") Long catId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page=attrGroupService.queryPage(params,catId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
