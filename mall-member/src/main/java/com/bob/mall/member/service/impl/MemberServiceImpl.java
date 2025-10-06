package com.bob.mall.member.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.mall.member.vo.MemberLoginVO;
import com.bob.mall.member.vo.SocialUserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.member.dao.MemberDao;
import com.bob.mall.member.entity.MemberEntity;
import com.bob.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public MemberEntity login(MemberLoginVO vo) {
        // 1.根据账号或者手机号来查询会员信息
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>()
                .eq("username", vo.getUserName())
                .or()
                .eq("mobile", vo.getUserName()));
        if(entity != null){
            // 2.如果账号或者手机号存在 然后根据密码加密后的校验来判断是否登录成功
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//            System.out.println(vo.getPassword());
//            System.out.println(entity.getPassword());
            boolean matches = encoder.matches(vo.getPassword(), entity.getPassword());
            if(matches){
                // 表明登录成功
                return entity;
            }
        }
        return null;
    }

    /**
     * 社交登录
     * @param vo
     * @return
     */
    @Override
    public MemberEntity sociallogin(SocialUserVO vo) {
        String uid = vo.getId();
        // 如果该用户是第一次社交登录，那么需要注册
        // 如果不是第一次社交登录 那么就更新相关信息 登录功能
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity != null){
            // 说明当前用户已经注册过了 更新token和过期时间
            MemberEntity entity = new MemberEntity();
            entity.setId(memberEntity.getId());
            return memberEntity;
        }
        // 表示用户是第一提交，那么我们就需要对应的来注册
        MemberEntity entity = new MemberEntity();
        entity.setUsername(vo.getName());
        entity.setEmail(vo.getEmail());
        entity.setSocialUid(vo.getId());

        // 注册用户信息
        this.save(entity);
        return entity;
    }

}