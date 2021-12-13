package com.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * user:lufei
 * DATE:2021/12/13
 **/
@Controller
public class helloController {

    @GetMapping("/{page}.html")
    public String hello(@PathVariable("page") String page) {
        return page;
    }
}
