package com.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/21
 **/
@Data
public class MergeVo {
    //items: [1, 2]
    //purchaseId: 1
    private Long purchaseId;
    private List<Long> items;
}
