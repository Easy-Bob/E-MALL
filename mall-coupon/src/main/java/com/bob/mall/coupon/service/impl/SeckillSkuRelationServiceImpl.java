package com.bob.mall.coupon.service.impl;

import com.bob.common.utils.PageUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.coupon.dao.SeckillSkuRelationDao;
import com.bob.mall.coupon.entity.SeckillSkuRelationEntity;
import com.bob.mall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
        String promotionSessionId =  (String) params.get("promotionSessionId");
        String key = (String)params.get("key");
        queryWrapper.eq(StringUtils.isNotBlank(promotionSessionId), "promotion_session_id", promotionSessionId)
                    .eq(StringUtils.isNotBlank(key), "sku_id", key);
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}