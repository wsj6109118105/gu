package com.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/12/28
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockLockedTo {

    /**
     * 库存工作单的id
     */
    private Long id;
    /**
     * 工作单详情id
     */
    private StockDetailTo detail;


}
