package com.cart.service.Impl;

import com.cart.service.cartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Slf4j
@Service
public class cartServiceImpl implements cartService {

    @Autowired
    StringRedisTemplate redisTemplate;
}
