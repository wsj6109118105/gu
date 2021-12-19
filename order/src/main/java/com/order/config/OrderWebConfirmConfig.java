package com.order.config;

import com.order.interceptor.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * user:lufei
 * DATE:2021/12/15
 **/
@Configuration
public class OrderWebConfirmConfig implements WebMvcConfigurer {

    @Autowired
    LoginUser loginUser;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUser).addPathPatterns("/**");
    }
}
