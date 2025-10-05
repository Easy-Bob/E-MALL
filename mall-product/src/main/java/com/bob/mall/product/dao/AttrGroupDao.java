package com.bob.mall.product.dao;

import com.bob.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bob.mall.product.vo.SpuItemGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemGroupAttrVo> getAttrGroupWithSpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
