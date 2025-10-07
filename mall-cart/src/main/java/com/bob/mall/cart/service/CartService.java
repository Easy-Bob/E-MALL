package com.bob.mall.cart.service;

import com.bob.mall.cart.vo.Cart;
import com.bob.mall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 购物车的Service接口
 */
public interface CartService {

    Cart getCartList();

    CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException, Exception;

    CartItem getCartItem(Long skuId);

    List<CartItem> getUserCartItems();
}
