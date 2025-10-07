package com.bob.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车中的商品信息
 */
@Data
public class CartItem {

    // 商品的编号 SkuId
    private Long skuId;

    private Long spuId;
    // 商品的图片
    private String image;
    // 商品的标题
    private String title;
    // 是否选中
    private boolean check = true;
    // 商品的销售属性
    private List<String> skuAttr;
    // 商品的单价
    private BigDecimal price;
    // 购买的数量
    private Integer count;
    // 商品的总价
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        // 商品的总价  price * count
        return price.multiply(new BigDecimal(count));
    }

}