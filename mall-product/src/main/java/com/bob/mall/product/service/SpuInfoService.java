package com.bob.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.product.entity.SpuInfoEntity;
import com.bob.mall.product.vo.OrderItemSpuInfoVO;
import com.bob.mall.product.vo.SpuInfoVO;

import java.util.List;
import java.util.Map;

/**
 * spu
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 21:25:58
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    void save(SpuInfoVO spuInfoVO);
    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(Long[] spuIds);
}



