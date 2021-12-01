package com.authServer.controller;

import com.authServer.config.OAuth2ConfigurationProperties;
import com.authServer.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/** 处理社交登录请求
 * user:lufei
 * DATE:2021/11/30
 **/
@Controller
@EnableConfigurationProperties(OAuth2ConfigurationProperties.class)
public class OAuth2Controller {

    @Autowired
    OAuth2ConfigurationProperties OAuth;

    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        // 根据 code 换取 accessToken
        Map<String,String> map = new HashMap<String,String>();
        map.put("client_id",OAuth.getClient_id());
        map.put("client_secret",OAuth.getClient_secret());
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.happymall.mall/oauth2/weibo/success");
        map.put("code",code);
        HttpResponse post = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map,new HashMap<>());
        // 登录成功跳回首页
        return "redirect:http://happymall.mall";
    }
}
