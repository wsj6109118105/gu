package com.order.feign;

import com.order.vo.MemberAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/19
 **/
@FeignClient("member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddress> getAddress(@PathVariable("memberId") Long memberId);
}
