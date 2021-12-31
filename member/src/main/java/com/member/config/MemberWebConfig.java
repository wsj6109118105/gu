package com.member.config;

import com.member.interceptor.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * user:lufei
 * DATE:2021/12/31
 **/
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUser loginUser;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUser).addPathPatterns("/**");
    }
}
