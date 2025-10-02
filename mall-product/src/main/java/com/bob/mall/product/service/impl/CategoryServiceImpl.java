package com.bob.mall.product.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import com.bob.mall.product.service.CategoryBrandRelationService;
import com.bob.mall.product.vo.Catalog2VO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
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

    @Transactional
    @Override
    public void updateDetail(CategoryEntity entity) {
        this.updateById(entity);
        if(!StringUtils.isEmpty(entity.getName())){
            categoryBrandRelationService.updateCatelogName(entity.getCatId(), entity.getName());

        }
    }

    @Override
    public List<CategoryEntity> getLevelCategory() {
        List<CategoryEntity> list = baseMapper.queryLevelCategory();
        return list;
    }

    /**
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     * @return
     */
    @Override
    public Map<String, List<Catalog2VO>> getCatelog2JSON() {
        // 获取所有二级和三级分类的数据
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        // 获取所有的一级分类的数据
        List<CategoryEntity> leve1Category =this.queryByParentCid(list, 0l);
        // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
        Map<String, List<Catalog2VO>> map = leve1Category.stream().collect(Collectors.toMap(
                key -> key.getCatId().toString()
                , value -> {
                    // 根据一级分类的编号，查询出对应的二级分类的数据
                    List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
                    List<Catalog2VO> Catalog2VOs =null;
                    if(l2Catalogs != null){
                        Catalog2VOs = l2Catalogs.stream().map(l2 -> {
                            // 需要把查询出来的二级分类的数据填充到对应的Catelog2VO中
                            Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                            // 根据二级分类的数据找到对应的三级分类的信息
                            List<CategoryEntity> l3Catelogs = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", catalog2VO.getId()));
                            if(l3Catelogs != null){
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