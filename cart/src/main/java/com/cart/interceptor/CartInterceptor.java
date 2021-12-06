package com.cart.interceptor;

import com.cart.vo.UserInfoTo;
import com.common.constant.AuthConstant;
import com.common.constant.CartConstant;
import com.common.vo.MemberResponseVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/** 再执行目标方法之前判断用户登录状态，并封装传递给 controller 目标请求
 * user:lufei
 * DATE:2021/12/6
 **/
@Component
public class CartInterceptor implements HandlerInterceptor {


    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();   // 传递给 controller 的信息
        HttpSession session = request.getSession();
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (member!=null) {
            // 用户登录
            userInfoTo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CartConstant.CART_USER_KEY.equals(name)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        // 第一次如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        threadLocal.set(userInfoTo);

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()) {

            Cookie cookie = new Cookie(CartConstant.CART_USER_KEY, userInfoTo.getUserKey());
            cookie.setDomain("happymall.mall");
            cookie.setMaxAge(CartConstant.CART_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
