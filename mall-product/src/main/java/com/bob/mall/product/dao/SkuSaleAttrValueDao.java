package com.bob.mall.product.dao;

import com.bob.mall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bob.mall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:59
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSkuSaleAttrValueBySpuId(@Param("spuId") Long spuId);
}
