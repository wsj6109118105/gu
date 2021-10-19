package com.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * user:lufei
 * DATE:2021/10/19
 **/
@Data
public class SpuBoundsTo {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}
