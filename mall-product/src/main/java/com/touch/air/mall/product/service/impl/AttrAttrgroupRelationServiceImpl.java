package com.touch.air.mall.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.product.dao.AttrAttrgroupRelationDao;
import com.touch.air.mall.product.entity.AttrAttrgroupRelationEntity;
import com.touch.air.mall.product.service.AttrAttrgroupRelationService;
import com.touch.air.mall.product.vo.AttrGroupRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addBatch(List<AttrGroupRelationVO> attrGroupRelationVOS) {
        if (CollUtil.isNotEmpty(attrGroupRelationVOS)) {
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrGroupRelationVOS.stream().map(attrGroupRelationVO -> {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                BeanUtils.copyProperties(attrGroupRelationVO, attrAttrgroupRelationEntity);
                return attrAttrgroupRelationEntity;
            }).collect(Collectors.toList());
            this.saveBatch(attrAttrgroupRelationEntities);
        }
    }

}