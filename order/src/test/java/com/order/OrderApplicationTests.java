package com.order;

import com.order.entity.OrderReturnApplyEntity;
import com.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class OrderApplicationTests {

    /**
     * 1.如何创建 exchange , queue , 绑定关系
     *      1),使用 amqpAdmin 进行创建
     */
    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void createExchange() {
        amqpAdmin.declareExchange(new DirectExchange("hello.java.exchange"));
        log.info("交换机打印完成");
    }

    @Test
    void createQueue() {
        amqpAdmin.declareQueue(new Queue("hello.java.queue"));
        log.info("队列创建完成");
    }

    @Test
    void binding() {
        amqpAdmin.declareBinding(new Binding("hello.java.queue", Binding.DestinationType.QUEUE,"hello.java.exchange","hello.java.queue",null));
        log.info("关系绑定完成");
    }

    @Test
    void sendMessage() {
        // 如果发送的消息是个对象，会使用序列化机制，将对象写出去
        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
        orderReturnReasonEntity.setId(1L);
        orderReturnReasonEntity.setCreateTime(new Date());
        orderReturnReasonEntity.setName("test");
        orderReturnReasonEntity.setStatus(0);
        orderReturnReasonEntity.setSort(1);
        String msg = "hello world!";
        for (int i = 0;i<10;i++) {
            rabbitTemplate.convertAndSend("hello.java.exchange","hello.java.queue",orderReturnReasonEntity);
        }
        log.info("消息发送完成{}",orderReturnReasonEntity);
    }

}
