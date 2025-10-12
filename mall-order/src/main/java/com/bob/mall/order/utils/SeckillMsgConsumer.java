package com.bob.mall.order.utils;
import com.alibaba.fastjson.JSON;
import com.bob.common.constant.OrderConstant;
import com.bob.mall.order.dto.SeckillOrderDto;
import com.bob.mall.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@RocketMQMessageListener(topic = OrderConstant.ROCKETMQ_SECKILL_ORDER_TOPIC, consumerGroup = "${rocketmq.consumer.group}")
@Component
public class SeckillMsgConsumer implements RocketMQListener<String> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(String s) {
        // 订单关单的逻辑实现
        SeckillOrderDto orderDto = JSON.parseObject(s, SeckillOrderDto.class);
        orderService.quickCreateOrder(orderDto);
    }
}
