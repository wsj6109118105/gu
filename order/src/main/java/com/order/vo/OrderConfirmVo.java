package com.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** 订单确认页需要用的数据
 * user:lufei
 * DATE:2021/12/15
 **/

public class OrderConfirmVo {

    // 收货地址
    @Getter @Setter
    List<MemberAddress> addresses;

    // 所有选中的购物项
    @Getter @Setter
    List<OrderItemVo> items;

    // todo 发票。。。

    // 订单防重令牌
    @Getter @Setter
    String orderToken;

    // 优惠卷,积分
    @Getter @Setter
    Integer integration;

    /**
     * 获取商品的总件数
     * @return 返回商品总件数
     */
    public Integer getCount() {
        assert items!=null;
        Integer count = 0;
        for (OrderItemVo item : items) {
            count += item.getCount();
        }
        return count;
    }
    // 订单总额
    //BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        assert items!=null;
        for (OrderItemVo item : items) {
            sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount())));
        }
        return sum;
    }
    // 优惠总额
    //BigDecimal reduce;

    // 应付价格
    //BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
