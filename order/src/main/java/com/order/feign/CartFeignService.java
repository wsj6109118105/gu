package com.order.feign;

import com.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/19
 **/
@FeignClient("cart")
public interface CartFeignService {

    @GetMapping("/currentUserItems")
    List<OrderItemVo> getCartItems();
}
