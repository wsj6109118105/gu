package com.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/** 整个购物车
 * user:lufei
 * DATE:2021/12/5
 **/

public class Cart {
    private List<CartItem> items;   // 购物项
    private Integer countNum;   //商品数量
    private Integer countType;   // 商品类型
    private BigDecimal totalAmount; // 总价
    private BigDecimal reduce = new BigDecimal(0); // 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        countNum=0;
        if (items!=null && items.size()>0) {
            for (CartItem item : items) {
                countNum+=item.getCount();
            }
        }
        return countNum;
    }

    public Integer getCountType() {
        if (items!=null && items.size()>0) {
            countType = items.size();
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        totalAmount = new BigDecimal(0);
        // 计算购物项总价
        if (items!=null && items.size()>0) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        // 减去优惠
        totalAmount = totalAmount.subtract(getReduce());
        return totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
