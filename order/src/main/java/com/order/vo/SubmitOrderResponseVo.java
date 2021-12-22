package com.order.vo;

import com.order.entity.OrderEntity;
import lombok.Data;

/** 提交订单返回数据的封装
 * user:lufei
 * DATE:2021/12/22
 **/
@Data
public class SubmitOrderResponseVo {
    /**
     * 订单实体类
     */
    private OrderEntity orderEntity;

    /**
     * 错误状态码
     * 0 : 成功
     */
    private Integer code;
}
