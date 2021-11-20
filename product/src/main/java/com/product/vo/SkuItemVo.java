package com.product.vo;

import com.product.entity.SkuImagesEntity;
import com.product.entity.SkuInfoEntity;
import com.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/11/17
 **/
@Data
public class SkuItemVo {
    // sku基本信息获取    pms_sku_info
    SkuInfoEntity Info;

    boolean hasStock = true;
    // sku的图片信息    pms_sku_images
    List<SkuImagesEntity> images;
    // spu 销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    // spu 介绍
    SpuInfoDescEntity desc;
    // spu 规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

}
