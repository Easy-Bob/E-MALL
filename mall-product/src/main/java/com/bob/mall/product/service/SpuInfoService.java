package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

