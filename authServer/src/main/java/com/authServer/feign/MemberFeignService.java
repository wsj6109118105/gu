package com.authServer.feign;

import com.authServer.vo.SocialUser;
import com.authServer.vo.UserLoginVo;
import com.authServer.vo.UserRegisterVo;
import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * user:lufei
 * DATE:2021/11/25
 **/
@FeignClient("member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R Login(@RequestBody SocialUser socialUser);
}
