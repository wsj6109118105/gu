package com.third.controller;

import com.common.utils.R;
import com.third.service.SendMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * user:lufei
 * DATE:2021/11/24
 **/
@RestController
public class SendMessageController {

    @Autowired
    SendMessageService service;

    /**
     * 提供给别的服务调用
     * @return
     */
    @GetMapping("/send")
    public R sendMessage(@RequestParam("PhoneNumber") String PhoneNumber,@RequestParam("code") String code) {
        service.sendMessage(PhoneNumber,code);
        return R.ok();
    }

}
