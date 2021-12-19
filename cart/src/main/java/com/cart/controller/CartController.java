package com.cart.controller;

import com.cart.interceptor.CartInterceptor;
import com.cart.service.cartService;
import com.cart.vo.Cart;
import com.cart.vo.CartItem;
import com.cart.vo.UserInfoTo;
import com.common.constant.AuthConstant;
import com.common.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * user:lufei
 * DATE:2021/12/6
 **/
@Controller
public class CartController {

    @Autowired
    cartService service;

    @ResponseBody
    @GetMapping("/currentUserItems")
    public List<CartItem> getCartItems() {
        return service.getCartItems();
    }

    /**
     * 展示购物车
     * @param model
     * @return 返回购物车信息
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = service.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        service.addToCart(skuId,num);
//        model.addAttribute("skuId",skuId);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.happymall.mall/addToCartSuccess.html";
    }

    /**
     * 跳转到成功页
     * @param skuId 商品id
     * @param model
     * @return 商品信息
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId,Model model) {
        // 重定向到成功页面
        CartItem cartItem = service.GetCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

    /**
     * 勾选购物项
     * @param skuId 商品id
     * @param check 商品是否被勾选
     * @return 重定向到购物车页面
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check) {
        service.checkItem(skuId,check);
        return "redirect:http://cart.happymall.mall/cart.html";
    }

    /**
     * 添加购物车中商品数量
     * @param skuId 商品id
     * @param count 修改的数量
     * @return 重定向到购物车页面
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("count") Integer count) {
        service.countItem(skuId,count);
        return "redirect:http://cart.happymall.mall/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        service.deleteItem(skuId);
        return "redirect:http://cart.happymall.mall/cart.html";
    }
}
