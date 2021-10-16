package com.product.vo;

import lombok.Data;

/**
 * user:lufei
 * DATE:2021/10/16
 **/
@Data
public class AttrRespVo extends AttrVo{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

}
