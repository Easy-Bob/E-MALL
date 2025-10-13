package com.bob.mall.order.utils;
import com.bob.common.constant.OrderConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


@RocketMQMessageListener(topic = OrderConstant.ROCKETMQ_ORDER_TOPIC, consumerGroup = "${rocketmq.consumer.group}")
@Component
public class OrderMsgConsumer implements RocketMQListener<String> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String s) {

    }
}
