package com.cart.controller;

import com.cart.interceptor.CartInterceptor;
import com.cart.service.cartService;
import com.cart.vo.CartItem;
import com.cart.vo.UserInfoTo;
import com.common.constant.AuthConstant;
import com.common.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Controller
public class CartController {

    @Autowired
    cartService service;

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
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            Model model) throws ExecutionException, InterruptedException {

        CartItem cartItem = service.addToCart(skuId,num);
        model.addAttribute("item",cartItem);
        return "success";
    }

}
