/**
  * Copyright 2021 json.cn
  */
package com.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

/**
 * Auto-generated: 2021-10-19 14:15:22
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class Skus {

    private List<Attr> attr;
    private String skuName;    //
    private BigDecimal price;   //
    private String skuTitle;    //
    private String skuSubtitle;    //
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
