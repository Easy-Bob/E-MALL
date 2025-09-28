package com.bob.mall.product.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.bob.mall.product.dao.CategoryDao;
import com.bob.mall.product.entity.CategoryEntity;
import com.bob.mall.product.service.CategoryService;


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