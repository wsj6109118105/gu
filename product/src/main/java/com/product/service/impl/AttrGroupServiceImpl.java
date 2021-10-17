package com.product.service.impl;

import com.product.dao.AttrAttrgroupRelationDao;
import com.product.entity.AttrAttrgroupRelationEntity;
import com.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.product.dao.AttrGroupDao;
import com.product.entity.AttrGroupEntity;
import com.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> Wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            Wrapper.and(obj->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if(catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    Wrapper);
            return new PageUtils(page);
        }else {
            Wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    Wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] attr) {
        //relationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()).eq("attr_group_id",));
        //只发送一次请求
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(attr).stream().map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelation);
            return attrAttrgroupRelation;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(collect);

    }
}
