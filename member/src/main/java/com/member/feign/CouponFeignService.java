package com.member.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * user:lufei
 * DATE:2021/10/4
 **/

//这是一个远程调用
@FeignClient("coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/categorybounds/member/list")
    R membercoupon();
}
