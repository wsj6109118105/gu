package com.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * user:lufei
 * DATE:2021/12/19
 **/
@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
         return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1. RequestContextHolder拿到刚进来的这个请求数据
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes!=null) {
                    HttpServletRequest request = requestAttributes.getRequest();  // 老请求
                    // 2. 同步请求头信息，cookie
                    String cookie = request.getHeader("Cookie");
                    // 给新请求同步了老请求 cookie
                    requestTemplate.header("Cookie",cookie);
                }
            }
        };
    }
}
