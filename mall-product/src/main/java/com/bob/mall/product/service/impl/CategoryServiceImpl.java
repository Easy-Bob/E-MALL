package com.bob.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import com.bob.mall.product.service.CategoryBrandRelationService;
import com.bob.mall.product.vo.Catalog2VO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.bob.mall.product.dao.CategoryDao;
import com.bob.mall.product.entity.CategoryEntity;
import com.bob.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    /**
     *
     * @param params
     * @return
     */
    @Override
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params) {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> list = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    categoryEntity.setChildrens(getCategoryChildrens(categoryEntity, categoryEntities));
                    return categoryEntity;
                }).sorted((e1, e2) -> {
                    return e1.getSort() == null ? 0 : (e1.getSort() - (e2.getSort() == null ? 0 : e2.getSort()));
    }).collect(Collectors.toList());
        return list;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

//    @CacheEvict(value = "category", key = "'getLevelCategory'")
    @Caching(evict = {
        @CacheEvict(value = "category", key = "'getLevelCategory'"),
        @CacheEvict(value = "category", key = "'getCatelog2JSON'")
    })
    @Transactional
    @Override
    public void updateDetail(CategoryEntity entity) {
        this.updateById(entity);
        if(!StringUtils.isEmpty(entity.getName())){
            categoryBrandRelationService.updateCatelogName(entity.getCatId(), entity.getName());

        }
    }

    // 当前方法的结果在分区的缓存中查询/存储
    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevelCategory() {
        List<CategoryEntity> list = baseMapper.queryLevelCategory();
        return list;
    }

    /**
     * 使用Spring Cache查询相关分类
     * @return
     */
    @Cacheable(value = "category", key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2VO>> getCatelog2JSON() {
        // 获取所有二级和三级分类的数据
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        // 获取所有的一级分类的数据
        List<CategoryEntity> leve1Category = this.queryByParentCid(list, 0l);
        // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
        Map<String, List<Catalog2VO>> map = leve1Category.stream().collect(Collectors.toMap(
                key -> key.getCatId().toString()
                , value -> {
                    // 根据一级分类的编号，查询出对应的二级分类的数据
                    List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
                    List<Catalog2VO> Catalog2VOs = null;
                    if (l2Catalogs != null) {
                        Catalog2VOs = l2Catalogs.stream().map(l2 -> {
                            // 需要把查询出来的二级分类的数据填充到对应的Catelog2VO中
                            Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                            // 根据二级分类的数据找到对应的三级分类的信息
                            List<CategoryEntity> l3Catelogs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", catalog2VO.getId()));
                            if (l3Catelogs != null) {
                                // 获取到的二级分类对应的三级分类的数据
                                List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catelogs.stream().map(l3 -> {
                                    Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                                    return catalog3VO;
                                }).collect(Collectors.toList());
                                // 三级分类关联二级分类
                                catalog2VO.setCatalog3List(catalog3VOS);
                            }
                            return catalog2VO;
                        }).collect(Collectors.toList());
                    }

                    return Catalog2VOs;
                }
        ));
        return map;
    }


    /**
     * 业务应用代码
     * 借助Redis + Redisson 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatelog2JSONDeprecated() {
        String key = "catalogJSON";
        // 从Redis中获取分类的信息
        String catalogJSON = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(catalogJSON)){
            // 缓存中没有数据，需要从数据库中查询
            Map<String, List<Catalog2VO>> catelog2JSONForDb = getCatelog2JSONForDb();
            if(catelog2JSONForDb == null){
                stringRedisTemplate.opsForValue().set(key, "1", 5, TimeUnit.SECONDS);
            }else{
                // 从数据库中查询到的数据，我们需要给缓存中也存储一份
                // 防止缓存雪崩
                String json = JSON.toJSONString(catelog2JSONForDb);
                stringRedisTemplate.opsForValue().set("catalogJSON", json, new Random().nextInt(10) + 1, TimeUnit.HOURS);
            }
            return catelog2JSONForDb;
        }
        // 如果缓存存在数据，从缓存中获取数据，然后返回
        Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
        });
        return stringListMap;
    }

    /**
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     * @return
     */
    private Map<String, List<Catalog2VO>> getCatelog2JSONForDb() {
        String keys = "catalogJSON";
        synchronized (this) {
            return getCatelog2JSONDbWithRedisson();
        }
    }

    private Map<String, List<Catalog2VO>> getCatelog2JSONDbWithRedisson(){
        String keys = "catalogJSON";
        RLock lock = redissonClient.getLock("catalog2JSON-lock");
        Map<String, List<Catalog2VO>> data = null;
        try{
            lock.lock();
            data = getDataForDb(keys);
        }finally {
            lock.unlock();
        }
        return data;
    }


    private Map<String, List<Catalog2VO>> getCatelog2JSONDbWithRedisLock(){
        String keys = "catalogJSON";
        // 加锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,300, TimeUnit.SECONDS);

        if(lock){
            Map<String, List<Catalog2VO>> data = null;
            try{
                data = getDataForDb(keys);
            }finally {
                String srcipts = "if redis.call('get',KEYS[1]) == ARGV[1]  then return redis.call('del',KEYS[1]) else  return 0 end ";
                // 通过Redis的lua脚本实现 查询和删除操作的原子性
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(srcipts,Long.class)
                        ,Arrays.asList("lock"),uuid);
            }
            return data;
        }else{
            // 加锁失败
            // 休眠 + 重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            return getCatelog2JSONDbWithRedisLock();
        }
    }

    // 单纯从redis/数据库中获取数据，然后存到redis
    private Map<String, List<Catalog2VO>> getDataForDb(String keys){
        // 先去缓存中查询有没有数据，如果有就返回，否则查询数据库
        // 从Redis中获取分类的信息
        String catalogJSON = stringRedisTemplate.opsForValue().get(keys);
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 说明缓存命中
            // 表示缓存命中了数据，那么从缓存中获取信息，然后返回
            Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
            return stringListMap;
        }

//            查询数据库操作
        // 获取所有二级和三级分类的数据
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        // 获取所有的一级分类的数据
        List<CategoryEntity> leve1Category = this.queryByParentCid(list, 0l);
        // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
        Map<String, List<Catalog2VO>> map = leve1Category.stream().collect(Collectors.toMap(
                key -> key.getCatId().toString()
                , value -> {
                    // 根据一级分类的编号，查询出对应的二级分类的数据
                    List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
                    List<Catalog2VO> Catalog2VOs = null;
                    if (l2Catalogs != null) {
                        Catalog2VOs = l2Catalogs.stream().map(l2 -> {
                            // 需要把查询出来的二级分类的数据填充到对应的Catelog2VO中
                            Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                            // 根据二级分类的数据找到对应的三级分类的信息
                            List<CategoryEntity> l3Catelogs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", catalog2VO.getId()));
                            if (l3Catelogs != null) {
                                // 获取到的二级分类对应的三级分类的数据
                                List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catelogs.stream().map(l3 -> {
                                    Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                                    return catalog3VO;
                                }).collect(Collectors.toList());
                                // 三级分类关联二级分类
                                catalog2VO.setCatalog3List(catalog3VOS);
                            }
                            return catalog2VO;
                        }).collect(Collectors.toList());
                    }

                    return Catalog2VOs;
                }
        ));
        if(map == null){
            // 那就说明数据库中也不存在  防止缓存穿透
            stringRedisTemplate.opsForValue().set(keys,"1",5, TimeUnit.SECONDS);
        }else{
            // 从数据库中查询到的数据，我们需要给缓存中也存储一份
            // 防止缓存雪崩
            String json = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set("catalogJSON",json,10,TimeUnit.MINUTES);
        }
        return map;
    }

    private List<CategoryEntity> queryByParentCid(List<CategoryEntity> list, Long parentCid){
        List<CategoryEntity> collect = list.stream().filter(item -> {
            return item.getParentCid().equals(parentCid);
        }).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if(entity.getParentCid() != 0){
            findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getCategoryChildrens(CategoryEntity categoryEntity
            , List<CategoryEntity> categoryEntities){
        List<CategoryEntity> collect = categoryEntities.stream().filter(entity -> {
            return entity.getParentCid().equals(categoryEntity.getCatId());
        }).map(entity -> {
            entity.setChildrens(getCategoryChildrens(entity, categoryEntities));
            return entity;
        }).sorted((e1, e2) -> {
            return e1.getSort() == null ? 0 : (e1.getSort() - (e2.getSort() == null ? 0 : e2.getSort()));
        }).collect(Collectors.toList());
        return collect;

    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        int res = baseMapper.deleteBatchIds(idList);
        return res > 0;
    }
}