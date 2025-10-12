package com.bob.mall.seckill.controller;

import com.bob.common.constant.SeckillConstant;
import com.bob.common.utils.R;
import com.bob.common.vo.MemberVO;
import com.bob.mall.seckill.dto.SeckillSkuRedisDto;
import com.bob.mall.seckill.interceptor.AuthInterceptor;
import com.bob.mall.seckill.service.SecKillService;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SecKillService secKillService;


    @ResponseBody
    @GetMapping("/getCurrentSeckillSessionSkus")
    public R getCurrentSeckillSessionSkus(){
        List<SeckillSkuRedisDto> currentSecKillSkus = secKillService.getCurrentSecKillSkus();
        return R.ok().put("data", JSON.toJSONString(currentSecKillSkus));
    }

    @ResponseBody
    @GetMapping("/seckillSessionBySkuId")
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId){
        SeckillSkuRedisDto dto = secKillService.getSeckillSessionBySkuId(skuId);
        return R.ok().put("data", JSON.toJSONString(dto));
    }

    /**
     * 秒杀抢购
     * @return
     * http://seckill.bob.com/seckill/kill?
     * killId=2_1&code=d5859076627a47d7961f9bc4e29b89a4&num=1
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                     @RequestParam("code") String code,
                     @RequestParam("num") Integer num,
                          Model model){

        // 1. 校验是否登录(AuthInterceptor)

        // 2. 秒杀服务
        String orderSN = secKillService.kill(killId, code, num);

        model.addAttribute("orderSN",orderSN);
        return "success";
    }
}
