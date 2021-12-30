package com.order.web;

import com.alipay.api.AlipayApiException;
import com.order.config.AlipayTemplate;
import com.order.service.OrderService;
import com.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * user:lufei
 * DATE:2021/12/30
 **/
@Controller
public class payWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 支付功能接口
     * @param orderSn 订单号
     * @return 直接返回支付宝返回的页面，并且再成功后跳转到用户的订单列表页
     * @throws AlipayApiException
     */
    @ResponseBody()
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        //将此页面直接交给浏览器
        return pay;
    }
}
