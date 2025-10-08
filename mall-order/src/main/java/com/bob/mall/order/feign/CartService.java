package com.bob.mall.order.feign;

import com.bob.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartService {
    @GetMapping("/getUserCartItems")
    public List<OrderItemVo> getUserCartItems();

}
