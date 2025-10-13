package com.bob.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装支付需要的相关信息
 */
@Data
public class PayVo {
    public PayVo(){

    }

    public PayVo(String out_trader_no, String subject, String total_amount, String body) {
        this.out_trader_no = out_trader_no;
        this.subject = subject;
        BigDecimal totalAmount = new BigDecimal(total_amount);
        this.total_amount = new BigDecimal(totalAmount.multiply(new BigDecimal(100)).longValue());
        this.body = body;
    }

    // 商户订单号
    private String out_trader_no;
    // 订单名称
    private String subject;
    // 付款金额
    private BigDecimal total_amount;
    // 描述
    private String body;

}
