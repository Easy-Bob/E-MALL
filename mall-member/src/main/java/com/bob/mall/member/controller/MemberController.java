package com.bob.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bob.common.exception.BizCodeEnume;
import com.bob.mall.member.vo.MemberLoginVO;
import com.bob.mall.member.vo.SocialUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bob.mall.member.entity.MemberEntity;
import com.bob.mall.member.service.MemberService;
import com.bob.common.utils.*;



/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 23:21:28
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginVO vo){
        MemberEntity entity = memberService.login(vo);
        if(entity != null){
            return R.ok();
        }

        return R.error(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getCode(),
                BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getMsg());
    }

    @RequestMapping("/sociallogin")
    public R socialLogin(@RequestBody SocialUserVO vo){
        MemberEntity entity = memberService.sociallogin(vo);
        if(entity != null){
            return R.ok();
        }

        return R.error(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getCode(),
                BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION.getMsg());
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
