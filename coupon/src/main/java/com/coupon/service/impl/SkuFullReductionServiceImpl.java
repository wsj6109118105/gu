package com.coupon.service.impl;

import com.common.to.MemberPrice;
import com.common.to.SkuReductionTo;
import com.coupon.entity.MemberPriceEntity;
import com.coupon.entity.SkuLadderEntity;
import com.coupon.service.MemberPriceService;
import com.coupon.service.SkuLadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.coupon.dao.SkuFullReductionDao;
import com.coupon.entity.SkuFullReductionEntity;
import com.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    MemberPriceService memberPriceService;

    @Autowired
    SkuLadderService skuLadderService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveReduction(SkuReductionTo skuReductionTo) {
        //1.保存满减表    ------>sms_sku_full_reduction(满减表)
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
        skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
        skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
        if(skuReductionTo.getFullPrice().compareTo(new BigDecimal(0))==1){
            this.baseMapper.insert(skuFullReductionEntity);
        }
        //2.保存打折表     ------>sms_sku_ladder(打折表)
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount()>0){
            skuLadderService.save(skuLadderEntity);
        }
        //3.保存会员优惠信息    --------->sms_member_price(会员价格表)

        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(price -> {
            MemberPriceEntity PriceEntity = new MemberPriceEntity();
            PriceEntity.setSkuId(skuReductionTo.getSkuId());
            PriceEntity.setMemberLevelName(price.getName());
            PriceEntity.setMemberPrice(price.getPrice());
            PriceEntity.setMemberLevelId(price.getId());
            PriceEntity.setAddOther(1);
            return PriceEntity;
        }).filter(item->{
            return item.getMemberPrice().compareTo(new BigDecimal(0))==1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}
