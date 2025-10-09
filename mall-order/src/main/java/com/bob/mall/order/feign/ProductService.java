package com.bob.mall.order.feign;

import com.bob.common.utils.R;
import com.bob.mall.order.vo.OrderItemSpuInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "mall-product")
public interface ProductService {
    @GetMapping("/product/brand/all")
    public R queryAllBrand();

    @RequestMapping("/product/spuinfo/getOrderItemSpuInfoBySpuId/{spuIds}")
    public List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(@PathVariable("spuIds") Long[] spuIds);
    }
