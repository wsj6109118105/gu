package com.seckill.config;

import com.seckill.interceptor.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * user:lufei
 * DATE:2022/1/10
 **/
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUser loginUser;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUser).addPathPatterns("/**");
    }
}
