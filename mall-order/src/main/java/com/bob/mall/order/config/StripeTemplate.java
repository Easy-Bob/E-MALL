package com.bob.mall.order.config;


import com.bob.mall.order.vo.PayVo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Card number	Expiry	CVC	Description
 * 4242 4242 4242 4242	Any future date	Any 3 digits	Success payment
 * 4000 0000 0000 9995	Any future date	Any 3 digits	Declined payment
 * 4000 0000 0000 0341	Any future date	Any 3 digits	Authentication required (3D Secure)
 */

@Component
@Data
public class StripeTemplate {
    @Value("${stripe.secret-key}")
    private String secretKey;
    @Value("${stripe.publishable-key}")
    private String publishableKey;
    @Value("${stripe.success-url}")
    private String successUrl;
    @Value("${stripe.cancel-url}")
    private String cancelUrl;
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;  // For webhook signature verification

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.secretKey;  // Initialize Stripe SDK
    }

    public String pay(PayVo payVo) throws StripeException {
        // Initialize Stripe
        Stripe.apiKey = secretKey;

        // Create checkout session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(payVo.getOut_trader_no()) // Store your order SN
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount((payVo.getTotal_amount().longValue() * 100)) // cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(payVo.getSubject())
                                                                .setDescription(payVo.getBody())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        // Return the Checkout URL
        return session.getUrl();
    }
}
