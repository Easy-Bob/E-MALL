package com.bob.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 23:21:28
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

