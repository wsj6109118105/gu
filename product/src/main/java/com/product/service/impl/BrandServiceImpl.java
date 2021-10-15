package com.product.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.product.dao.BrandDao;
import com.product.entity.BrandEntity;
import com.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");

        if(!"".equals(key)){
            QueryWrapper<BrandEntity> Wrapper = new QueryWrapper<>();
            Wrapper.like("name",key).or().like("descript",key).or().eq("brand_id",key);
            IPage<BrandEntity> page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    Wrapper
            );
            return new PageUtils(page);
        }else {
            IPage<BrandEntity> page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    new QueryWrapper<BrandEntity>()
            );
            return new PageUtils(page);
        }



    }

}
