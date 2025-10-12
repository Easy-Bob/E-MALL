package com.bob.mall.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundsDTO {

    private long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
