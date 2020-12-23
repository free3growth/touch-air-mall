package com.touch.air.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.product.entity.AttrGroupEntity;

import java.util.Map;

/**
 * 属性分组
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catId);
}

