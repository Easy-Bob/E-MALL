package com.bob.mall.order.service.impl;

import com.bob.common.utils.PageUtils;
import com.bob.common.vo.MemberVO;
import com.bob.mall.order.Interceptor.AuthInterceptor;
import com.bob.mall.order.feign.CartService;
import com.bob.mall.order.feign.MemberService;
import com.bob.mall.order.feign.ProductService;
import com.bob.mall.order.vo.MemberAddressVo;
import com.bob.mall.order.vo.OrderConfirmVo;
import com.bob.mall.order.vo.OrderItemVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bob.common.utils.*;

import com.bob.mall.order.dao.OrderDao;
import com.bob.mall.order.entity.OrderEntity;
import com.bob.mall.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    @Autowired
    private MemberService memberFeignService;

    @Autowired
    private CartService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        // 获取RequestContextHolder的相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1. 用户的会员 地址信息
            Long id = memberVO.getId();
            List<MemberAddressVo> address = memberFeignService.getAddress(id);
            vo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 同步主线程中的 RequestContextHolder
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2. 购物车商品
            List<OrderItemVo> userCartItems = cartFeignService.getUserCartItems();
            vo.setItems(userCartItems);
        }, executor);

        // 3. 订单总金额 和 需要支付的金额

        try {
            CompletableFuture.allOf(addressFuture, cartFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return vo;

    }

}