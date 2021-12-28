package com.ware.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * user:lufei
 * DATE:2021/12/28
 **/
@FeignClient("order")
public interface OrderFeignService {

    @GetMapping("/gorder/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
