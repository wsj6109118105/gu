package com.authServer.controller;

import com.alibaba.fastjson.TypeReference;
import com.authServer.feign.MemberFeignService;
import com.authServer.vo.UserLoginVo;
import com.authServer.vo.UserRegisterVo;
import com.common.constant.AuthConstant;
import com.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * user:lufei
 * DATE:2021/11/25
 **/
@Controller
public class LoginController {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @PostMapping("/regist")
    public String register(@Valid UserRegisterVo user, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.happymall.mall/reg.html";
        }
        String code = user.getCode();
        String s = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + user.getPhone());
        if (!StringUtils.isEmpty(s) && s.split("_")[0].equals(code)) {
            // 删除验证码，令牌机制
            redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + user.getPhone());
            // 调用远程服务
            R register = memberFeignService.register(user);
            if (register.getCode() == 0) {   // 成功
                return "redirect:http://auth.happymall.mall/login.html";
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", register.getData("msg",new TypeReference<String>() {
                }));
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.happymall.mall/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码不正确");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.happymall.mall/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes) {
        // 远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode()==0) {
            return "redirect:http://happymall.mall";
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.happymall.mall/login.html";
        }
    }
}
