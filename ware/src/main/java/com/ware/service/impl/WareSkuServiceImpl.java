package com.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.common.exception.NoStockException;
import com.common.to.mq.StockDetailTo;
import com.common.to.mq.StockLockedTo;
import com.common.utils.R;
import com.rabbitmq.client.Channel;
import com.ware.dao.WareOrderTaskDetailDao;
import com.ware.entity.WareOrderTaskDetailEntity;
import com.ware.entity.WareOrderTaskEntity;
import com.ware.feign.OrderFeignService;
import com.ware.feign.ProductFeignService;
import com.ware.service.WareOrderTaskDetailService;
import com.ware.service.WareOrderTaskService;
import com.ware.vo.OrderItemVo;
import com.ware.vo.OrderVo;
import com.ware.vo.SkuHasStockVo;
import com.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.ware.dao.WareSkuDao;
import com.ware.entity.WareSkuEntity;
import com.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareOrderTaskDetailDao wareOrderTaskDetailDao;

    /**
     *
     * @param to
     */
    @Override
    public void handleStockLockedRelease(StockLockedTo to) throws IOException {

        Long id = to.getId();     //  库存工作单的 id
        StockDetailTo detail = to.getDetail();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detail.getId());
        if (taskEntity!=null) {
            // 不是因为锁定库存失败而回滚，那么需要释放锁定的库存
            // 解锁
            // 1.查询对应订单信息，
            //   1) 没有订单信息 ，必须解锁
            //   2) 有这个订单，需要判断订单状态
            //      订单状态：已取消 ：解锁
            //               没取消 ：不解锁
            String orderSn = taskEntity.getOrderSn();  // 根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode()==0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {});
                if (data == null || data.getStatus() == 4) {
                    // 订单不存在或者订单已经被取消了
                    System.out.println("收到解锁库存的消息");
                    if (byId.getLockStatus()==1) {
                        unlockStock(detail.getSkuId(), detail.getWareId(),detail.getSkuNum(),detail.getId());
                    }
                }
            }else {
                throw new RuntimeException("远程服务失败");
            }
        }else {
            // 因为库存锁定失败，库存回滚，无需解锁
        }
    }

    /**
     * 解锁库存方法
     * @param skuId 商品id
     * @param wareId 仓库id
     * @param num 商品锁定数量
     */
    private void unlockStock(Long skuId,Long wareId,Integer num,Long taskDetailId) {
        wareSkuDao.unlockStock(skuId,wareId,num);
        // 跟新库存工作单
        wareOrderTaskDetailDao.updateLockStatus(taskDetailId);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 添加库存
     * @param skuId 商品id
     * @param skuNum 商品数量
     * @param wareId 仓库id
     */
    @Override
    public void addSStock(Long skuId, Integer skuNum, Long wareId) {
        //判断如果没有这个记录，则新增
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO查询商品的名字，并设置
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                String sku_name = (String) data.get("skuName");
                wareSkuEntity.setSkuName(sku_name);
            } catch (Exception e) {

            }

            wareSkuDao.insert(wareSkuEntity);

        } else {
            wareSkuDao.addStock(skuId, skuNum, wareId);
        }
    }

    /**
     * 判断是否有库存
     * @param skuIds 需要锁定的商品的 skuId
     * @return 批量返回商品库存情况
     */
    @Override
    public List<SkuHasStockVo> getHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);

            Long count = wareSkuDao.hasStock(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据订单锁定库存,这个为一个事物,如果有一个锁定不成功,则都要回滚
     * 库存解锁场景：
     * 1) 下订单成功，但是过期未支付，订单自动取消，或被用户手动取消
     * 2) 库存锁定成功，但是其他业务出问题
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) throws NoStockException{
        // 保存库存工作单的详情
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        // 找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> stocks = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());
        // 锁定库存
        for (SkuWareHasStock stock : stocks) {
            boolean skuLocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功有 1 行受影响
                Long row = wareSkuDao.lockSkuStock(skuId, wareId, stock.num);
                if (row == 1) {
                    skuLocked = true;
                    // todo 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null,skuId,"",stock.num,taskEntity.getId(),wareId,1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                    break;
                }
            }
            if (!skuLocked) {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}
