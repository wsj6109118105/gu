package com.seckill.interceptor;

import com.common.constant.AuthConstant;
import com.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * user:lufei
 * DATE:2021/12/15
 **/
@Component
public class LoginUser implements HandlerInterceptor {


    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", requestURI);
        if (match) {
            MemberResponseVo attribute = (MemberResponseVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
            if (attribute!=null) {
                loginUser.set(attribute);
                return true;
            }else {
                System.out.println("请先登录");
                request.getSession().setAttribute("msg","请先登录");
                response.sendRedirect("http://auth.happymall.mall/login.html");
                return false;
            }
        }
        return true;
    }
}
