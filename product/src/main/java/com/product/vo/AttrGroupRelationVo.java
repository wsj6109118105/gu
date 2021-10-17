package com.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * user:lufei
 * DATE:2021/10/17
 **/
@Data
public class AttrGroupRelationVo {

    private Long attrId;

    private Long attrGroupId;
}
