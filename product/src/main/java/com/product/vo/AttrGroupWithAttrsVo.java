package com.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.product.entity.AttrEntity;
import com.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/18
 **/
@Data
public class AttrGroupWithAttrsVo {

    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
