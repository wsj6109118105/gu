package com.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 21:30:31
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

