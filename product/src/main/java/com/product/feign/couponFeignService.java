package com.product.feign;

import com.common.to.SkuReductionTo;
import com.common.to.SpuBoundsTo;
import com.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * user:lufei
 * DATE:2021/10/19
 **/
@FeignClient("coupon")
public interface couponFeignService {


    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveReduction(@RequestBody SkuReductionTo skuReductionTo);
}
