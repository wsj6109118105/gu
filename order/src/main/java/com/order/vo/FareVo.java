package com.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * user:lufei
 * DATE:2021/12/23
 **/
@Data
public class FareVo {
    private MemberAddress address;
    private BigDecimal fare;
}
