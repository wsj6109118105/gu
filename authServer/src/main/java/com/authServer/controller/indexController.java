package com.authServer.controller;

import com.authServer.feign.ThirdPartFeignService;
import com.common.constant.AuthConstant;
import com.common.exception.BizCodeException;
import com.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * user:lufei
 * DATE:2021/11/21
 **/
@RestController
public class indexController {

    @Autowired
    ThirdPartFeignService service;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        // TODO 接口防刷
        String s = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        System.out.println(s);
        if (s!=null&&!StringUtils.isEmpty(s)) {
            long l = Long.parseLong(s.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                // 60 秒内不能再发
                return R.error(BizCodeException.SMS_CODE_EXCEPTION.getCode(), BizCodeException.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 验证码的再次校验 redis
        if (s!=null&&!StringUtils.isEmpty(s)) {
            service.sendMessage(phone,s.split("_")[0]);
        }else {
            String code = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();
            redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX+phone,code,60, TimeUnit.SECONDS);
            service.sendMessage(phone,code.split("_")[0]);
        }
        return R.ok();
    }
}
