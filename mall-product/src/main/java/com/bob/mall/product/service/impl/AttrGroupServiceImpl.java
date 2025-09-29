package com.bob.mall.product.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import com.bob.mall.product.entity.AttrEntity;
import com.bob.mall.product.entity.CategoryEntity;
import com.bob.mall.product.service.AttrService;
import com.bob.mall.product.vo.AttrGroupWithAttrsVo;
import com.bob.mall.product.vo.AttrVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.bob.mall.product.dao.AttrGroupDao;
import com.bob.mall.product.entity.AttrGroupEntity;
import com.bob.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询列表数据
     *    根据列表编号来查询
     * @param params
     * @param catelogId 如何catelogId为0 就不根据catelogId来查询
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if (catelogId.equals(0)) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper
            );
            return new PageUtils(page);
        }
        wrapper.eq("catelog_id", catelogId);
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>()
                .getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrgroupWIthAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroups = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> list = attrGroups.stream().map((group) -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, vo);
            List<AttrEntity> attrEntities = attrService.getRelationAttr(group.getAttrGroupId());
            vo.setAttrs(attrEntities);
            return vo;
        }).collect(Collectors.toList());
        return list;
    }


}