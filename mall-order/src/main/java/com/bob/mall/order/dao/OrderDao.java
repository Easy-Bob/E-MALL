package com.bob.mall.order.dao;

import com.bob.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:34:48
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    OrderEntity getOrderByOrderSn(@Param("orderSn") String orderSn);

    void updateOrderStatus(@Param("orderSn") String orderSn,@Param("status") Integer status);
}
