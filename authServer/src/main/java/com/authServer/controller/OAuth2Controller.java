package com.authServer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.authServer.config.OAuth2ConfigurationProperties;
import com.authServer.feign.MemberFeignService;
import com.authServer.vo.MemberResponseVo;
import com.authServer.vo.SocialUser;
import com.common.utils.HttpUtils;
import com.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Controller
@EnableConfigurationProperties(OAuth2ConfigurationProperties.class)
public class OAuth2Controller {

    @Autowired
    OAuth2ConfigurationProperties OAuth;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登录回调
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        // 根据 code 换取 accessToken
        Map<String,String> map = new HashMap<String,String>();
        map.put("client_id",OAuth.getClient_id());
        map.put("client_secret",OAuth.getClient_secret());
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.happymall.mall/oauth2/weibo/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map,new HashMap<>());
        if (response.getStatusLine().getStatusCode()==200) {
            // 获取 access_token
            String s = EntityUtils.toString(response.getEntity());
            SocialUser user = JSON.parseObject(s,SocialUser.class);
            // 用户第一次登录网站需要注册
            // 需要判断是登录还是注册
            R login = memberFeignService.Login(user);
            if (login.getCode()==0) {
                MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {});
                //System.out.println("登录成功，用户信息" + data);
                log.info("登录成功，用户信息"+data.toString());
                return "redirect:http://happymall.mall";
            }else {
                return "redirect:http://auth.happymall.mall/login.html";
            }
        }else {
            return "redirect:http://auth.happymall.mall/login.html";
        }
        // 登录成功跳回首页
        //return "redirect:http://happymall.mall";
    }
}
