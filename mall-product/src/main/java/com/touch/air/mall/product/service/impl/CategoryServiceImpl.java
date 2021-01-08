package com.touch.air.mall.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.touch.air.common.utils.PageUtils;
import com.touch.air.common.utils.Query;
import com.touch.air.mall.product.dao.CategoryDao;
import com.touch.air.mall.product.entity.CategoryEntity;
import com.touch.air.mall.product.service.CategoryBrandRelationService;
import com.touch.air.mall.product.service.CategoryService;
import com.touch.air.mall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author bin.wang
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getFirstLevelCategroys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 首页分类 渲染二级、三级分类的数据
     * @return
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //1、查出所有1级分类
        List<CategoryEntity> firstLevelCategroys = getFirstLevelCategroys();
        //2、封装数据
        Map<String, List<Catalog2Vo>> map = firstLevelCategroys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), item -> {
            //2.1、查到这个一级分类下的所有二级分类
            List<CategoryEntity> category2Entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", item.getCatId()));
            List<Catalog2Vo> catalog2Vos = null;
            if (CollUtil.isNotEmpty(category2Entities)) {
                catalog2Vos = category2Entities.stream().map(categoryEntity2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(item.getCatId().toString(), null, categoryEntity2.getName(), categoryEntity2.getCatId().toString());
                    //2.2、查找当前二级分类下的三级分类
                    List<CategoryEntity> category3Entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", categoryEntity2.getCatId()));
                    List<Catalog2Vo.Catalog3Vo> catalog3VoList = category3Entities.stream().map(categoryEntity3 -> {
                        Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(categoryEntity2.getCatId().toString(), categoryEntity3.getName(), categoryEntity3.getCatId().toString());
                        return catalog3Vo;
                    }).collect(Collectors.toList());
                    catalog2Vo.setCatalog3List(catalog3VoList);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return map;
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