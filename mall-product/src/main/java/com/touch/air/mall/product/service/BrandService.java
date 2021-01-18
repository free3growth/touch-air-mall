package com.touch.air.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 更新保证冗余字段一致
     * @param brand
     */
    void updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

