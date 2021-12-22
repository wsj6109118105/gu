package com.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.common.utils.R;
import com.ware.feign.MemberFeignService;
import com.ware.vo.MemberAddress;
import com.ware.vo.fareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.ware.dao.WareInfoDao;
import com.ware.entity.WareInfoEntity;
import com.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)) {
            wrapper.eq("id",key).or().like("name",key).or().like("address",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public fareVo getFare(Long id) {
        fareVo FareVo = new fareVo();
        R info = memberFeignService.info(id);
        MemberAddress data = info.getData("memberReceiveAddress",new TypeReference<MemberAddress>() {
        });
        if (data!=null) {
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal fare = new BigDecimal(substring);
            FareVo.setFare(fare);
            FareVo.setAddress(data);
            return FareVo;
        }
        return null;
    }

}
