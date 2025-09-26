package com.bob.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:23:23
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

