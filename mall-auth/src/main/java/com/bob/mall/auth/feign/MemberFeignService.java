package com.bob.mall.auth.feign;

import com.bob.common.utils.R;
import com.bob.mall.auth.vo.LoginVo;
import com.bob.mall.auth.vo.SocialUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/login")
    R login(LoginVo loginVo);

    @PostMapping("/member/member/sociallogin")
    R socialLogin(SocialUser socialUser);

}
