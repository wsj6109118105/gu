package com.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * user:lufei
 * DATE:2021/11/3
 **/
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        //创建配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        //创建实例
        return Redisson.create(config);
    }
}
