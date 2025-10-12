package com.bob.mall.seckill.feign;

import com.bob.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {


    @GetMapping("/coupon/seckillsession/getLate3DaysSession")
    R getLate3DaysSession();

}
