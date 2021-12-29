package com.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.to.mq.OrderTo;
import com.common.to.mq.StockLockedTo;
import com.common.utils.PageUtils;
import com.rabbitmq.client.Channel;
import com.ware.entity.WareSkuEntity;
import com.ware.vo.LockStockResult;
import com.ware.vo.SkuHasStockVo;
import com.ware.vo.WareSkuLockVo;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 22:51:34
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addSStock(Long skuId, Integer skuNum, Long wareId);

    /**
     * 能否锁定库存锁定成功
     * @param skuIds 需要锁定的商品的 skuId
     * @return 成功返回，失败抛异常回滚
     */
    List<SkuHasStockVo> getHasStock(List<Long> skuIds);

    /**
     * 根据订单锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 解锁库存方法
     * @param to
     * @throws IOException
     */
    void handleStockLockedRelease(StockLockedTo to) throws IOException;

    void handleStockLockedRelease(OrderTo order) throws IOException;
}

