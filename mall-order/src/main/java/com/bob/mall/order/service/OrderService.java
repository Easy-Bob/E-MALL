package com.bob.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bob.common.utils.PageUtils;
import com.bob.mall.order.dto.SeckillOrderDto;
import com.bob.mall.order.entity.OrderEntity;
import com.bob.mall.order.feign.ProductService;
import com.bob.mall.order.vo.OrderConfirmVo;
import com.bob.mall.order.vo.OrderResponseVO;
import com.bob.mall.order.vo.OrderSubmitVO;
import com.bob.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 
 *
 * @author bob
 * @email bsun3217@gmail.com
 * @date 2025-09-25 22:34:48
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);


    OrderConfirmVo confirmOrder();


    OrderResponseVO submitOrder(OrderSubmitVO vo);

    void quickCreateOrder(SeckillOrderDto seckillOrderDto);

    PayVo getPayVo(String orderSn);

    void updateOrderStatus(String orderSn, Integer status);

    void handleOrderComplete(String orderSn, int code);
}

