package com.bob.mall.seckill.dto;

import com.bob.mall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisDto {
    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal seckillCount;
    private BigDecimal seckillLimit;
    private Integer seckillSort;
    private SkuInfoVo skuInfoVo;
    private Long PromotionSessionId;

    private Long startTime;
    private Long endTime;

    // 随机码
    private String randCode;
}
