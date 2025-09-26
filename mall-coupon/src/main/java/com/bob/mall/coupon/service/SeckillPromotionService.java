package com.bob.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.coupon.entity.SeckillPromotionEntity;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:23:24
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

