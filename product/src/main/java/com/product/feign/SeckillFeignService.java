package com.product.feign;

import com.common.utils.R;
import com.product.feign.fallback.SeckillFeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * user:lufei
 * DATE:2022/1/9
 **/
// 熔断措施 ，返回熔断数据
@FeignClient(name = "seckill",fallback = SeckillFeignServiceImpl.class)
public interface SeckillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
