package com.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/22
 **/
@Data
public class FinishVo {
    @NotNull
    private Long id;
    private List<item> items;
}
