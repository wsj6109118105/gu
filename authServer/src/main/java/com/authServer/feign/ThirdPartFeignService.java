package com.authServer.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * user:lufei
 * DATE:2021/11/24
 **/
@FeignClient("third")
public interface ThirdPartFeignService {

    @GetMapping("/send")
    R sendMessage(@RequestParam("PhoneNumber") String PhoneNumber, @RequestParam("code") String code);
}
