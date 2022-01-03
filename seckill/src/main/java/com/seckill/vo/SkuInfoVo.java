package com.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 商品的基本信息
 * user:lufei
 * DATE:2022/1/3
 **/
@Data
public class SkuInfoVo {
    private Long skuId;
    /**
     * spuId
     */
    private Long spuId;
    /**
     * sku名称
     */
    private String skuName;      //
    /**
     * sku介绍描述
     */
    private String skuDesc;
    /**
     * 所属分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 默认图片
     */
    private String skuDefaultImg;
    /**
     * 标题
     */
    private String skuTitle;      //
    /**
     * 副标题
     */
    private String skuSubtitle;      //
    /**
     * 价格
     */
    private BigDecimal price;       //
    /**
     * 销量
     */
    private Long saleCount;
}
