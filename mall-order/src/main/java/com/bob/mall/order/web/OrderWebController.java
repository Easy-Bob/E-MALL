package com.bob.mall.order.web;

import com.bob.common.constant.OrderConstant;
import com.bob.mall.order.config.StripeTemplate;
import com.bob.mall.order.entity.OrderItemEntity;
import com.bob.mall.order.service.OrderItemService;
import com.bob.mall.order.service.OrderService;
import com.bob.mall.order.vo.OrderConfirmVo;
import com.bob.mall.order.vo.OrderResponseVO;
import com.bob.mall.order.vo.OrderSubmitVO;
import com.bob.mall.order.vo.PayVo;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Card number	Expiry	CVC	Description
 * 4242 4242 4242 4242	Any future date	Any 3 digits	Success payment
 * 4000 0000 0000 9995	Any future date	Any 3 digits	Declined payment
 * 4000 0000 0000 0341	Any future date	Any 3 digits	Authentication required (3D Secure)
 */


@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private StripeTemplate stripeTemplate;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmVo", confirmVo);
        return "confirm";
    }

    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitVO vo, Model model){
        OrderResponseVO responseVO = orderService.submitOrder(vo);
        Integer code = responseVO.getCode();
        if(code == 0){
            model.addAttribute("orderResponseVO", responseVO);
            // 下单成功
            return "pay";
        }else{
            System.out.println("ORDER code = " + code);
            // 下单失败
            return "redirect:http://order.bob.com/toTrade";
        }
    }



    @GetMapping("/orderPay")
    @ResponseBody
    public void pay(@RequestParam("orderSn") String orderSn, HttpServletResponse response) throws Exception {
        PayVo payVo = orderService.getPayVo(orderSn);
        String checkoutUrl = stripeTemplate.pay(payVo);
        response.sendRedirect(checkoutUrl); // user goes directly to Stripe
    }

    @GetMapping("/pay/success")
    public String success(@RequestParam("session_id") String sessionId, Model model) throws Exception {
        // Retrieve session from Stripe
        Session session = Session.retrieve(sessionId);

        // Get order info
        String orderSn = session.getClientReferenceId();
        long amount = session.getAmountTotal(); // cents
//        String currency = session.getCurrency();

//        orderService.updateOrderStatus(orderSn, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());
        orderService.handleOrderComplete(orderSn, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());

        // get skuInfo
        OrderItemEntity orderItemEntity = orderItemService.getByOrderSn(orderSn);
        model.addAttribute("orderItem", orderItemEntity);
        model.addAttribute("totalAmount", amount / 100.0);
        return "list"; // Thymeleaf
    }

    @GetMapping("/pay/cancel")
    public String payCancel(){
//        orderService.updateOrderStatus(orderSn, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());
        return "redirect:/mall.bob.com"; // redirect to homepage
    }
}
