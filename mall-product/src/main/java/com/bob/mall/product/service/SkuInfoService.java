package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:59
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

