package com.order.feign;

import com.common.utils.R;
import com.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/21
 **/
@FeignClient("ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getHasStock(@RequestBody List<Long> skuIds);


    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long id);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
