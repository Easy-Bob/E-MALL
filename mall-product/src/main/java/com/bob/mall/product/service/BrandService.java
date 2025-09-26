package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.BrandEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Ʒ
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

