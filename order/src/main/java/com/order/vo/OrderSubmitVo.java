package com.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/** 封装订单数据的 vo
 * user:lufei
 * DATE:2021/12/22
 **/
@Data
@ToString
public class OrderSubmitVo {
    private Long addrId;  // 收货地址的 id
    private Integer payType;  // 支付方式
    // 无需提交需要购买的商品，去购物车在获取一边

    private String orderToken;  //  防重令牌
    private BigDecimal payPrice; // 应付总价
    private String note;  // 备注
    // 用户相关信息直接去 session中取
}
