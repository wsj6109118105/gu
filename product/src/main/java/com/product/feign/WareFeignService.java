package com.product.feign;

import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/27
 **/
@FeignClient("ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getHasStock(@RequestBody List<Long> skuIds);
}
