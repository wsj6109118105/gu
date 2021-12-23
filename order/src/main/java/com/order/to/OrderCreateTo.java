package com.order.to;

import com.order.entity.OrderEntity;
import com.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * user:lufei
 * DATE:2021/12/23
 **/
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;

}
