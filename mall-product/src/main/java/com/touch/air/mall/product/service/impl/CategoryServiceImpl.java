package com.touch.air.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.product.dao.CategoryDao;
import com.touch.air.mall.product.entity.CategoryEntity;
import com.touch.air.mall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author bin.wang
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查询出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2、组装成父子的树形结构
        // 2.1、找出所有的一级分类
        // 2.2、递归查出子类
        List<CategoryEntity> menuTree = categoryEntities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu-> {
            menu.setChildren(getChildrens(menu, categoryEntities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return menuTree;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);

    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //找父id并收集
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid()!=0){
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }


    /**
     * 递归查找所有菜单的子菜单
     */
    public List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}