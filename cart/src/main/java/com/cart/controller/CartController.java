package com.cart.controller;

import com.cart.interceptor.CartInterceptor;
import com.cart.vo.UserInfoTo;
import com.common.constant.AuthConstant;
import com.common.utils.Constant;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Controller
public class CartController {

    @GetMapping("/cart.html")
    public String cartListPage() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart() {

        return "success";
    }

}
