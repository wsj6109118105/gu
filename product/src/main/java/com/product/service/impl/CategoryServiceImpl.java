package com.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.product.service.CategoryBrandRelationService;
import com.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.product.dao.CategoryDao;
import com.product.entity.CategoryEntity;
import com.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /*
        查询目录并且以父子的树形结构展示
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //2.组装成父子的树形结构
        //  1 )找到所有的一级分类
        List<CategoryEntity> level1 = categoryEntities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1;
    }

    /*
        递归查找所有子目录
        @ param:root 父级目录
        @ param:all  所有的目录
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> collect = all.stream().filter((child) -> {
            return child.getParentCid() == root.getCatId();
        }).map(child -> {
            //递归调用
            child.setChildren(getChildren(child, all));
            return child;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }

    /*
        批量删除
        @ param:asList 需要删除目录集合
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 判断当前菜单是否被其他地方引用

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();


        List<Long> parent = findParent(catelogId, path);

        return parent.toArray(new Long[parent.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());


            //TODO 更新其他关联
        }
    }

    @Cacheable(value = {"category"},key = "'Level1Category'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("使用了springCache");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    /**
     * 使用缓存
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
        if (StringUtils.isEmpty(catelogJson)) {
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDBWithRedisson();

            return catelogJsonFromDB;
        }
        return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    //Redisson
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisson() {
        //加锁防止缓存击穿
        // 使用本地锁只能锁住当前进程，在分布式情况下，需要使用分布式锁。
        // 占分布式锁  原子加锁原子解锁
        // 锁的名字相同即为同一把锁，名字很重要
        RReadWriteLock catelogJsonLock = redissonClient.getReadWriteLock("CatelogJsonLock");
        RLock rLock = catelogJsonLock.readLock();
        rLock.lock();
        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            // 删除锁
            rLock.unlock();
        }
        return dataFromDB;

    }

    //redis 的分布式锁
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() {
        //加锁防止缓存击穿
        // 使用本地锁只能锁住当前进程，在分布式情况下，需要使用分布式锁。
        // 占分布式锁  原子加锁原子解锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {   //加锁成功
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";// lua脚本，用来释放分布式锁
                // 删除锁
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {     // 加锁失败，重试
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catelogJson = stringRedisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isEmpty(catelogJson)) {
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
            /*
                变为一次查询
            */
        List<CategoryEntity> categoryEntities2 = baseMapper.selectList(null);
        //1.查询1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(categoryEntities2, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 查到所有一级分类的   二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(categoryEntities2, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            //二级分类不为空
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    //找二级分类的三级分类
                    List<CategoryEntity> categoryEntities1 = getParent_cid(categoryEntities2, item.getCatId());
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryEntities1 != null) {
                        catelog3Vos = categoryEntities1.stream().map(i -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), i.getCatId().toString(), i.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catelog2Vo.setCatelog3List(catelog3Vos);
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catelogJson", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    //从数据库查询封装数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {
        //加锁防止缓存击穿
        // todo 使用本地锁只能锁住当前进程，在分布式情况下，需要使用分布式锁。
        synchronized (this) {
            return getDataFromDB();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities2, Long parent_cid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = categoryEntities2.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
    }

    public List<Long> findParent(Long parentId, List<Long> path) {
        if (parentId != 0) {
            CategoryEntity byId = this.getById(parentId);
            path = findParent(byId.getParentCid(), path);
            path.add(parentId);
        }
        return path;
    }
}
