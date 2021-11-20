package com.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/11/17
 **/
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
