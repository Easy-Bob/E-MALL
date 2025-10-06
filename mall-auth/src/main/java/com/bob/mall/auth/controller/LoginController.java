package com.bob.mall.auth.controller;

import com.bob.common.utils.R;
import com.bob.mall.auth.feign.MemberFeignService;
import com.bob.mall.auth.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private MemberFeignService memberFeginService;

    @PostMapping("/login")
    public String login(LoginVo loginVo , RedirectAttributes redirectAttributes,
                        HttpSession session){
        R r = memberFeginService.login(loginVo);
        if(r.getCode() == 0){
            // 表示登录成功
            session.setAttribute("loginUser", "bob_user");
            return "redirect:http://mall.bob.com";
        }

        redirectAttributes.addAttribute("errors",r.get("msg"));

        // 表示登录失败,重新跳转到登录页面
        return "redirect:http://auth.bob.com/login.html";
    }

}
