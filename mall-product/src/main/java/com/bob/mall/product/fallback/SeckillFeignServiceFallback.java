package com.bob.mall.product.fallback;

import com.bob.common.exception.BizCodeEnume;
import com.bob.common.utils.R;
import com.bob.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService{
    public R getSeckillSessionBySkuId(Long skuId) {
        log.error("Fallback...SeckillFeignService");
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
    }
}
