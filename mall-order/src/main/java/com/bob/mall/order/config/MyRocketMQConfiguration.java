package com.bob.mall.order.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRocketMQConfiguration {

    @Value("${rocketmq.name-server}")
    private String nameserver;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("rocketmq.consumer.group")
    private String consumerGroup;

    private String orderTopic = "orderTopic";

    public void handler(){

    }

    /**
     * 发送延迟消息
     */
    public void sendDelayMsg(String orderSN) throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameserver);

        // start producer
        int totalMessagesToSend = 100;
        for (int i = 0; i < totalMessagesToSend; i++) {
            Message message = new Message(orderTopic, orderSN.getBytes());
            message.setDelayTimeLevel(4); // 30

            // send
            producer.send(message);
        }
        // 关闭
        producer.shutdown();
    }
}
