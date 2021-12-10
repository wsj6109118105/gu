package com.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class OrderApplicationTests {

    /**
     * 1.如何创建 exchange , queue , 绑定关系
     *      1),使用 amqpAdmin 进行创建
     */
    @Autowired
    AmqpAdmin amqpAdmin;

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

}
