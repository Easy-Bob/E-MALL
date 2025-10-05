package com.bob.mall.product.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.common.utils.Query;
import com.bob.mall.product.vo.SkuItemSaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.bob.mall.product.dao.SkuSaleAttrValueDao;
import com.bob.mall.product.entity.SkuSaleAttrValueEntity;
import com.bob.mall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSkuSaleAttrValueBySpuId(Long spuId) {
        List<SkuItemSaleAttrVo>  attrsVo = skuSaleAttrValueDao.getSkuSaleAttrValueBySpuId(spuId);
        return attrsVo;

    }

}