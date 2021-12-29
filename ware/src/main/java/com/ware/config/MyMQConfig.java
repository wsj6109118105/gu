package com.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * user:lufei
 * DATE:2021/12/27
 **/
@Configuration
public class MyMQConfig {

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange StockEventExchange() {
        return new TopicExchange("stock-event-exchange",true,false);
    }

    /**
     * 一个普通的队列，将过期信息发送给消费者
     * @return
     */
    @Bean
    public Queue StockReleaseQueue() {
        return new Queue("stock.release.queue",true,false,false);
    }

    /**
     * 延时队列，保存需要延时的消息
     * @return
     */
    @Bean
    public Queue StockDelayQueue() {
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    /**
     * 绑定交换机与延时队列   路由键为   stock.locked
     * @return
     */
    @Bean
    public Binding StockLockedBinding() {
        //String destination, DestinationType destinationType, String exchange,
        // String routingKey,
        //        @Nullable Map<String, Object> arguments
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

    /**
     * 绑定交换机与普通队列    路由键为 stock.release.#
     * @return
     */
    @Bean
    public Binding StockReleaseBinding() {
        //String destination, DestinationType destinationType, String exchange,
        // String routingKey,
        //        @Nullable Map<String, Object> arguments
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }
}
