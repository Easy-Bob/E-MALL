package com.bob.mall.seckill.service;


import com.bob.mall.seckill.dto.SeckillSkuRedisDto;

import java.util.List;

public interface SecKillService {

    void uploadSecKillSku3Days();

    List<SeckillSkuRedisDto> getCurrentSecKillSkus();

    SeckillSkuRedisDto getSeckillSessionBySkuId(Long skuId);

    String kill(String killId, String code, Integer num);
}
