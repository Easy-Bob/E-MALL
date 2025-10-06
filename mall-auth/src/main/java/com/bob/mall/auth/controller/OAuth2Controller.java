package com.bob.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bob.common.utils.HttpUtils;
import com.bob.common.utils.R;
import com.bob.mall.auth.feign.MemberFeignService;
import com.bob.mall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vo.MemberVO;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

@Controller
public class OAuth2Controller {

    @Value("${client_id}")
    private String clientId;

    @Value("${client_secret}")
    private String clientSecret;

    @Value("${redirect_uri}")
    private String redirectURI;

    @Autowired
    private MemberFeignService memberFeignService;

    @RequestMapping("/oauth/google/success")
    public String GoogleOAuth(@RequestParam("code") String code,
                              HttpSession session,
                              HttpResponse response) throws Exception{
        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type","authorization_code");
        body.put("redirect_uri", redirectURI);
        body.put("code", code);

        // 获取 token
        HttpResponse post = HttpUtils.doPost(
                "https://oauth2.googleapis.com",
                "/token",
                "post",
                new HashMap<>(),
                null,
                body
        );

        if (post.getStatusLine().getStatusCode() != 200) {
            return "redirect:http://auth.bob.com/login.html";
        }

        String json = EntityUtils.toString(post.getEntity());
        JSONObject tokenObj = JSON.parseObject(json);
        String accessToken = tokenObj.getString("access_token");

        // 获取用户信息
        HttpResponse userInfoResp = HttpUtils.doGet(
                "https://www.googleapis.com",
                "/oauth2/v2/userinfo",
                "get",
                new HashMap<String, String>() {{
                    put("Authorization", "Bearer " + accessToken);
                }},
                null
        );

        String userJson = EntityUtils.toString(userInfoResp.getEntity());
        SocialUser socialUser = JSON.parseObject(userJson, SocialUser.class);
        R r = memberFeignService.socialLogin(socialUser);
        if(r.getCode() != 0){
            return "redirect:http://auth.bob.com/login.html";
        }
//        System.out.println(socialUser);
        session.setAttribute("loginUser", socialUser.getName());

        return "redirect:http://mall.bob.com";
    }

}
