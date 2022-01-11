package com.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/** RabbitMQ中只要有队列，即使属性发生变化也不会覆盖
 * user:lufei
 * DATE:2021/12/27
 **/
@Configuration
public class MyMQConfig {

    /**
     * 创建延时队列
     * @return
     */
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

    /**
     * 创建到时间后给服务发送消息的队列
     * @return
     */
    @Bean
    public Queue OrderReleaseQueue() {
        Queue queue = new Queue("order.release.queue",true,false,false);
        return queue;
    }

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange OrderEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange("order-event-exchange",true,false);
        return topicExchange;
    }

    /**
     * 绑定交换机与延时队列  路由键为 order.create.order
     * @return
     */
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

    /**
     * 绑定交换机与给服务发消息的队列   路由键为  order.release.order
     * @return
     */
    @Bean
    public Binding OrderReleaseOrderBinding() {
        return new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 绑定库存的普通队列   用来取消订单时解锁库存
     */
    @Bean
    public Binding OrderReleaseOtherBinding() {
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    /**
     * 秒杀服务队列
     */
    @Bean
    public Queue OrderSecKillOrderQueue() {
        Queue queue = new Queue("order.seckill.order.queue",true,false,false);
        return queue;
    }

    /**
     * 绑定秒杀队列与交换机
     */
    @Bean
    public Binding OrderSecKillOrderQueueBinding() {
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
