package com.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * user:lufei
 * DATE:2021/12/21
 **/
@Data
public class fareVo {
    private MemberAddress address;
    private BigDecimal fare;
}
