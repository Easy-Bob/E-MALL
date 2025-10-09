package com.bob.mall.order.dto;

import com.bob.mall.order.entity.OrderEntity;
import com.bob.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateTO {

    private OrderEntity orderEntity; // 订单信息
    private List<OrderItemEntity> orderItemEntitys; // 订单信息
}
