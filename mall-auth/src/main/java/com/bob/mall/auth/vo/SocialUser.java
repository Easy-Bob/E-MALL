package com.bob.mall.auth.vo;

import lombok.Data;

@Data
public class SocialUser {
//    Google 用户信息：{"name":"Sun Bob",
//    "id":"115812651735234035371",
//    "verified_email":true,
//    "given_name":"Sun",
//    "family_name":"Bob",
//    "email":"bsun3217@gmail.com",
//    "picture":"https://lh3.googleusercontent.com/a/ACg8ocIZRjyWeocWiLUejPj8ejmksigVsmZzvvQiKFFOd77mP27azA=s96-c"}

    private String name;
    private String id;
    private String email;

}
