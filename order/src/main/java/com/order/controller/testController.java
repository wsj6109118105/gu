package com.order.controller;

import com.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * user:lufei
 * DATE:2021/12/11
 **/
@Slf4j
@RestController
public class testController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMessage")
    public void sendMessage() {
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
