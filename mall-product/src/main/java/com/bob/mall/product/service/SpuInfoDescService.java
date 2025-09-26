package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:59
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

