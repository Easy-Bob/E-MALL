package com.bob.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:34:48
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderItemEntity getByOrderSn(String orderSn);
}

