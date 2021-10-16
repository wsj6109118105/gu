package com.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.product.entity.AttrEntity;
import com.product.vo.AttrRespVo;
import com.product.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author lufei
 * @email 2362487738@qq.com
 * @date 2021-10-03 21:30:31
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);
}

