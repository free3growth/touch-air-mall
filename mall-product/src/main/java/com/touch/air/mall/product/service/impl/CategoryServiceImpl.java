package com.touch.air.mall.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author bin.wang
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

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
        ).map(menu -> {
            menu.setChildren(getChildrens(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
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

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     * @CacheEvict:失效模式的使用
     */
    //@CacheEvict(value = {"category"},key = "'level1Categorys'")
    @Caching(
            evict = {
                    @CacheEvict(value = "category", key = "'level1Categorys'"),
                    @CacheEvict(value = "category", key = "'getCatalogJson'")
            }
    )
    //@CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {

        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * @Cacheable
     * 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 每一个需要缓存的数据，我们都来指定要放到哪个名字的缓存【缓存的分区（推荐按照业务类型分）】
     *
     * @return
     */
    @Cacheable(value = {"category"},key = "'level1Categorys'",sync = true)
    @Override
    public List<CategoryEntity> getFirstLevelCategroys() {
        System.out.println("getFirstLevelCategroys....");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        System.out.println("查询了数据库...");
        /**
         * 1、将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1、查出所有1级分类
        List<CategoryEntity> firstLevelCategroys = getParent_cid(selectList, 0L);
        //2、封装数据
        Map<String, List<Catalog2Vo>> map = firstLevelCategroys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), item -> {
            //2.1、查到这个一级分类下的所有二级分类
            List<CategoryEntity> category2Entities = getParent_cid(selectList, item.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (CollUtil.isNotEmpty(category2Entities)) {
                catalog2Vos = category2Entities.stream().map(categoryEntity2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(item.getCatId().toString(), null, categoryEntity2.getName(), categoryEntity2.getCatId().toString());
                    //2.2、查找当前二级分类下的三级分类
                    List<CategoryEntity> category3Entities = getParent_cid(selectList, categoryEntity2.getCatId());
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

    /**
     * 首页分类 渲染二级、三级分类的数据
     * redis缓存
     * <p>
     * SpringBoot2.3 暂时未出现该问题
     * TODO 产生堆外内存溢出：OutOfDirectMemoryError
     * 1、springboot2.0 以后默认使用Lettuce作为操作redis的客户端，它使用netty进行网络通信
     * 2、lettuce的bug导致堆外内存溢出
     * 解决方案：由于是lettuce的bug造成，不能直接使用-Dio.netty.maxDirectMemory去调大虚拟机堆外内存
     * 1)、升级lettuce客户端。   2）、切换使用jedis
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonOld() {
        //给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型：【序列化与反序列化】
        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */
        // 1、加入缓存逻辑
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StrUtil.isNotEmpty(catalogJson)) {
            System.out.println("缓存命中...直接返回...");
            //转为指定的对象
            Map<String, List<Catalog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return map;
        } else {
            //2、缓存中没有,查询数据库
            System.out.println("缓存不命中...查询数据库...");
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();
            //3、查询的数据再放入缓存，将对象转为json放入缓存
            stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(catalogJsonFromDB), 1, TimeUnit.DAYS);
            return catalogJsonFromDB;
        }
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性
     *  1. 双写模式
     *  2. 失效模式
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        //1、锁的名字 锁的粒度，越细越快
        // 锁的粒度：具体缓存的是某个数据，11号商品 product-11-lock,product-12-lock 与 product-lock
        RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        catalogJsonLock.lock();
        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            catalogJsonLock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        //1、占分布式锁，去redis占位置
        //1.1、setIfAbsent 就是redis的 setNX命令
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功....");
            //加锁成功
            //设置锁的过期时间，避免死锁，必须和加锁是同步的、原子的
            //stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            //业务执行成功，删除锁
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //删除锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            //获取值对比+对比成功删除 这两步也必须是原子操作
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                //删除我自己的锁
//                stringRedisTemplate.delete("lock");
//            }

            return dataFromDb;
        } else {
            //加锁失败
            //休眠1000ms重试 Thread.sleep(1000)
            //自旋
            System.out.println("获取分布式锁失败...等待重试...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StrUtil.isNotEmpty(catalogJson)) {
            //如果缓存不为null，直接返回
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库");
        /**
         * 1、将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1、查出所有1级分类
        List<CategoryEntity> firstLevelCategroys = getParent_cid(selectList, 0L);
        //2、封装数据
        Map<String, List<Catalog2Vo>> map = firstLevelCategroys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), item -> {
            //2.1、查到这个一级分类下的所有二级分类
            List<CategoryEntity> category2Entities = getParent_cid(selectList, item.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (CollUtil.isNotEmpty(category2Entities)) {
                catalog2Vos = category2Entities.stream().map(categoryEntity2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(item.getCatId().toString(), null, categoryEntity2.getName(), categoryEntity2.getCatId().toString());
                    //2.2、查找当前二级分类下的三级分类
                    List<CategoryEntity> category3Entities = getParent_cid(selectList, categoryEntity2.getCatId());
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
        //将查询到的数据，转为json 存入缓存
        stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(map), 1, TimeUnit.DAYS);
        return map;
    }

    /**
     * 首页分类 渲染二级、三级分类的数据
     * 从数据库查询并封装整个分类数据
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1、synchronized(this):SpringBoot所有的组件在容器中都是单例的
        //TODO 本地锁：synchronized,JUC(Lock)。在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //找父id并收集
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }


    /**
     * 递归查找所有菜单的子菜单
     */
    public List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}