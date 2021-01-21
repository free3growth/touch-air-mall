package com.touch.air.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.touch.air.mall.product.entity.AttrGroupEntity;
import com.touch.air.mall.product.vo.SpuItemGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemGroupAttrVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
