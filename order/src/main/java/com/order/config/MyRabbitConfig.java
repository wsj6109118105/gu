package com.order.config;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * user:lufei
 * DATE:2021/12/11
 **/
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 使用 JSON 序列化机制来序列化消息
     * @return JSON 转化器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制 RabbitTemplate
     * @return 定制的 RabbitTemplate
     */
    @PostConstruct    // 构造器完成之后调用这个方法
    public void initRabbitTemplate() {

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息唯一关联数据(这是消息的唯一id)
             * @param b
             * @param s
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                // 服务器收到了消息
                /**
                 * 做好消息确认机制 ，(publish,consumer)
                 * 每一个消息再数据库做好记录，定期发送失败的消息
                 */
                //System.out.println("correlationData.."+correlationData+"...b..."+b+"...s..."+s);
            }
        });   // 设置确认回调

        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                // 服务报错了，修改数据库，当前消息的状态-> 错误
                System.out.println(returnedMessage);
            }
        });
    }
}
