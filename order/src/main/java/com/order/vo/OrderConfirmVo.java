package com.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 订单确认页需要用的数据
 * user:lufei
 * DATE:2021/12/15
 **/
@Data
public class OrderConfirmVo {

    // 收货地址
    List<MemberAddress> addresses;

    // 所有选中的购物项
    List<OrderItemVo> items;

    // todo 发票。。。


    // 优惠卷
    Integer integration;

    // 订单总额
    BigDecimal total;

    // 优惠总额
    BigDecimal reduce;

    // 应付价格
    BigDecimal payPrice;
}
