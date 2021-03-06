package com.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.product.entity.BrandEntity;
import com.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-15 15:42:43
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

