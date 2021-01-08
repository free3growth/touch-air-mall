package com.touch.air.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.mall.product.entity.CategoryEntity;
import com.touch.air.mall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author bin.wang
 * @email 1178321785@qq.com
 * @date 2020-12-04 13:18:33
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询所有分类，层次树形结构
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 批量删除菜单
     * @param asList
     */
    void removeMenusByIds(List<Long> asList);

    /**
     * 找到catelogId的完整路径
     * 【父/子/孙子/后代】
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getFirstLevelCategroys();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

