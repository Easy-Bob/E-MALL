package com.bob.mall.order.web;

import com.bob.mall.order.service.OrderService;
import com.bob.mall.order.vo.OrderConfirmVo;
import com.bob.mall.order.vo.OrderResponseVO;
import com.bob.mall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;
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
}
