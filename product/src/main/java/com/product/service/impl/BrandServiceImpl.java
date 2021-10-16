package com.product.service.impl;

import com.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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

    /**
     * 品牌跟新，修改关系表中对应的品牌
     * @param brand
     */
    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证冗余字段数据一致
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            //同步更新其他关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());


            //TODO 更新其他关联
        }
    }

}
