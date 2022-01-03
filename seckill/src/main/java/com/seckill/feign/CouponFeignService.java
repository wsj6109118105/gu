package com.seckill.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * user:lufei
 * DATE:2022/1/3
 **/
@FeignClient("coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();
}
