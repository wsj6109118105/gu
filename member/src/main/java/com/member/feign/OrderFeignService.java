package com.member.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * user:lufei
 * DATE:2021/12/31
 **/
@FeignClient("order")
public interface OrderFeignService {

    @PostMapping("/gorder/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
