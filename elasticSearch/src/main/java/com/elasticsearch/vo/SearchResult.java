package com.elasticsearch.vo;

import com.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/11/7
 **/
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModel> product;

    private Integer pageNum;   //当前页码
    private Long total;    // 总数
    private Integer totalPages;  // 总页码

    private List<BrandVo> brands;   //当前查询到的结果所有涉及到的品牌

    private List<AttrVo> attrs;   //当前查询到的结果所有涉及到的属性

    private List<CatalogVo> catalogs;   //当前查询到的结果所有涉及到的分类

    @Data
    public static class BrandVo {
        private Long brandId;    // 品牌id
        private String brandName;   // 品牌名字
        private String brandImg;   // 图片
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
