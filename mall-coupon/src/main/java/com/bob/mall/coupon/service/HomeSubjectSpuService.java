package com.bob.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * ר
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:23:24
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

