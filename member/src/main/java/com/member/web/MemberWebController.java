package com.member.web;

import com.alibaba.fastjson.JSON;
import com.common.utils.R;
import com.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * user:lufei
 * DATE:2021/12/31
 **/
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String member(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum
                                    ,Model model) {
        Map<String,Object> param = new HashMap<>();
        param.put("page",pageNum.toString());
        R r = orderFeignService.listWithItem(param);
        model.addAttribute("orders",r.get("page"));
        //System.out.println(JSON.toJSONString(r.get("page")));
        // 查出当前登录用户的所有订单数据
        return "orderList";
    }
}
