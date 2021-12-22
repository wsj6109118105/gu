package com.order.controller;

import com.order.feign.MemberFeignService;
import com.order.service.OrderService;
import com.order.vo.OrderConfirmVo;
import com.order.vo.OrderSubmitVo;
import com.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;

/**
 * user:lufei
 * DATE:2021/12/15
 **/
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 生成订单页信息
     * @param model
     * @return 返回订单数据
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirm",confirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     * @param submitVo 用户提交的数据
     * @return 成功返回支付页面，失败返回订单页面
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
        if (responseVo.getCode()==0) {
            // 成功
            return "pay";
        }else {
            return "redirect:http://order.happymall.mall/toTrade";
        }
    }
}
