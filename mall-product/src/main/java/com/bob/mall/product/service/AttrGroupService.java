package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.AttrGroupEntity;
import com.bob.mall.product.vo.AttrGroupWithAttrsVo;
import com.bob.mall.product.vo.SpuItemGroupAttrVo;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);
    PageUtils queryPage(Map<String, Object> params, Long catelogId);


    List<AttrGroupWithAttrsVo> getAttrgroupWIthAttrsByCatelogId(Long catelogId);

    List<SpuItemGroupAttrVo> getAttrGroupWithSpuId(Long spuId, Long catalogId);
}

