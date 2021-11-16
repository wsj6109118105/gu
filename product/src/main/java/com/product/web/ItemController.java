package com.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * user:lufei
 * DATE:2021/11/16
 **/

@Controller
public class ItemController {

    /**
     *  展示当前 sku 详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId) {

        System.out.println("准备查询 " + skuId +" 详情");
        return "item";
    }
}
