package com.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 购物项
 * user:lufei
 * DATE:2021/12/5
 **/
@Data
public class CartItem {
    private Long skuId;
    private Boolean check = true;    // 是否选中
    private String title;
    private String img;
    private List<String> skuAttr;    // 套餐信息
    private BigDecimal price;     // 单价
    private Integer count;     //数量
    private BigDecimal totalPrice;    // 总价

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(""+this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
