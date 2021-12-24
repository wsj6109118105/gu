package com.order.vo;

import lombok.Data;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/24
 **/
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 需要锁住的所有库存
     */
    private List<OrderItemVo> locks;
}
