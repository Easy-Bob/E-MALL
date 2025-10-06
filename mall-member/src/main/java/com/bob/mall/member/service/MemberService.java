package com.bob.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.member.entity.MemberEntity;
import com.bob.mall.member.vo.MemberLoginVO;
import com.bob.mall.member.vo.SocialUserVO;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 23:21:28
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberEntity login(MemberLoginVO vo);

    MemberEntity sociallogin(SocialUserVO vo);
}

