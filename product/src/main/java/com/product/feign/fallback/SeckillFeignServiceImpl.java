package com.product.feign.fallback;

import com.common.exception.BizCodeException;
import com.common.utils.R;
import com.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * user:lufei
 * DATE:2022/1/17
 **/
@Slf4j
@Component
public class SeckillFeignServiceImpl implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("调用熔断方法");
        return R.error(BizCodeException.TOO_MANY_REQUEST.getCode(), BizCodeException.TOO_MANY_REQUEST.getMsg());
    }
}
