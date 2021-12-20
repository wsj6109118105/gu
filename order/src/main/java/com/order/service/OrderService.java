package com.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.order.entity.OrderEntity;
import com.order.vo.OrderConfirmVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 23:16:36
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     *
     * @return 返回订单需要用的数据
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;
}

