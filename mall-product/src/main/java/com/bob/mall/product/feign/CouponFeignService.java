package com.bob.mall.product.feign;

import com.bob.common.dto.SkuReductionDTO;
import com.bob.common.dto.SpuBoundsDTO;
import com.bob.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/skufullreduction/saveinfo")
    public R saveFullReductionInfo(@RequestBody SkuReductionDTO dto);

    @RequestMapping("/coupon/spubounds/saveSpuBounds")
    R saveSpuBounds(@RequestBody SpuBoundsDTO spuBoundsDTO);
}
