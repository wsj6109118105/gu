package com.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/15
 **/
@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String img;
    private List<String> skuAttr;    // 套餐信息
    private BigDecimal price;     // 单价
    private Integer count;     //数量
    private BigDecimal totalPrice;    // 总价
}
