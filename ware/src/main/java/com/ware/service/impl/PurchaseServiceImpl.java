package com.ware.service.impl;

import com.common.constant.WareConstant;
import com.ware.entity.PurchaseDetailEntity;
import com.ware.service.PurchaseDetailService;
import com.ware.service.WareSkuService;
import com.ware.vo.FinishVo;
import com.ware.vo.MergeVo;
import com.ware.vo.item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.ware.dao.PurchaseDao;
import com.ware.entity.PurchaseEntity;
import com.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.print.attribute.standard.PrinterURI;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status",0).or().eq("status",1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 合并采购需求单
     * @param mergeVo
     */
    @Transactional
    @Override
    public int merge(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // TODO 确认采购单状态是0或者1
        PurchaseEntity byId = this.getById(purchaseId);
        if(!(byId.getStatus()<WareConstant.PurchaseStatusEnum.RECEIVE.getCode())){
            return 0;
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
        return 1;
    }

    /**
     * 领取采购单
     * @param ids
     */
    @Override
    public void received(List<Long> ids) {
        // 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(x -> {
            return x.getStatus() < WareConstant.PurchaseStatusEnum.FINISH.getCode();
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 改变采购单的状态
        this.updateBatchById(collect);

        // 改变采购需求相对应的状态
        collect.forEach(item->{
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BYING.getCode());
                return entity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void done(FinishVo finishVo) {
        // 改变采购单中每一个采购项的状态
        Boolean flag = true;
        List<item> items = finishVo.getItems();
        List<PurchaseDetailEntity> update = new ArrayList<>();
        for (item item : items) {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(item.getItemId());
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                entity.setStatus(item.getStatus());
            }else {
                entity.setStatus(item.getStatus());
                // 查出采购项
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());

                //进行入库
                wareSkuService.addSStock(byId.getSkuId(),byId.getSkuNum(),byId.getWareId());
            }
            update.add(entity);
        }
        purchaseDetailService.updateBatchById(update);
        // 改变采购单状态
        Long id = finishVo.getId();
        PurchaseEntity byId = this.getById(id);
        if(flag==true){
            byId.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());
        }else {
            byId.setStatus(WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        }
        byId.setUpdateTime(new Date());
        this.updateById(byId);
    }
}
