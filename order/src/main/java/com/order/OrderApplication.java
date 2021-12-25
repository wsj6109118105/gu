package com.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 引入 rabbitMQ
 *
 * 本地事务失效问题：
 *  同一个对象内事务方法互调默认失效，原因 绕过了代理对象，事务使用代理对象来控制的
 *  解决：
 *      1) 引入 aop-starter , 引入 aspectj
 *      2) @EnableAspectJAutoProxy(exposeProxy = true); 开启 aspectj动态代理功能 对外暴露代理对象
 *      3) 用代理对象本类互调
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRabbit
@EnableFeignClients
@EnableRedisHttpSession
@MapperScan("com/order/dao")
@EnableDiscoveryClient
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
