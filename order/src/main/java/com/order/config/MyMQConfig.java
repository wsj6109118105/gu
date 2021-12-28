package com.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/** RabbitMQ中只要有队列，即使属性发生变化也不会覆盖
 * user:lufei
 * DATE:2021/12/27
 **/
@Configuration
public class MyMQConfig {

    @Bean
    public Queue OrderDelayQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        //@Nullable Map<String, Object> arguments
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue",true,false,false,arguments);
        return queue;
    }

    @Bean
    public Queue OrderReleaseQueue() {
        Queue queue = new Queue("order.release.queue",true,false,false);
        return queue;
    }

    @Bean
    public Exchange OrderEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange("order-event-exchange",true,false);
        return topicExchange;
    }

    @Bean
    public Binding OrderCreateOrderBinding() {
        //String destination, DestinationType destinationType, String exchange,
        // String routingKey,
        //        @Nullable Map<String, Object> arguments
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding OrderReleaseOrderBinding() {
        return new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }
}
