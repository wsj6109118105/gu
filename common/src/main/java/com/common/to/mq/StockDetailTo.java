package com.common.to.mq;

import lombok.Data;

/**
 * user:lufei
 * DATE:2021/12/28
 **/
@Data
public class StockDetailTo {
    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 锁定状态  1-锁定    2-解锁     3-扣减
     */
    private Integer lockStatus;
}
