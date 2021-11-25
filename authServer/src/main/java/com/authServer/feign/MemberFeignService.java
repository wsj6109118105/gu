package com.authServer.feign;

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
}