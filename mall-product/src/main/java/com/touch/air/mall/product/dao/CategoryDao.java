package com.touch.air.mall.product.dao;

import com.touch.air.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
