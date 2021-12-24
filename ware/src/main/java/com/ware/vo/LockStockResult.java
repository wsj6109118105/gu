package com.ware.vo;

import lombok.Data;

/**
 * user:lufei
 * DATE:2021/12/24
 **/
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
