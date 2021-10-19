package com.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/19
 **/
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
