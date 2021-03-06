package com.product.service.impl;

import com.product.dao.AttrAttrgroupRelationDao;
import com.product.entity.AttrAttrgroupRelationEntity;
import com.product.entity.AttrEntity;
import com.product.service.AttrService;
import com.product.vo.AttrGroupRelationVo;
import com.product.vo.AttrGroupWithAttrsVo;
import com.product.vo.SpuItemAttrGroupVo;
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

    @Autowired
    AttrService attrService;
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
        //?????????????????????
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(attr).stream().map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelation);
            return attrAttrgroupRelation;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(collect);

    }

    /**
     * ???????????? id?????????????????????????????????????????????????????????????????????
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //????????????
        List<AttrGroupEntity> group = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //??????????????????
        List<AttrGroupWithAttrsVo> collect = group.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            /*//????????????????????????????????????
            List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", item.getAttrGroupId()));
            //?????????attr_id
            List<Long> attrId = relationEntities.stream().map(relationItem -> {
                return relationItem.getAttrId();
            }).collect(Collectors.toList());
            //?????? attr_id ???????????????
            List<AttrEntity> attr_id = attrService.list(new QueryWrapper<AttrEntity>().in("attr_id", attrId));
            attrGroupWithAttrsVo.setAttrs(attr_id);*/
            List<AttrEntity> attrs = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());


        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1. ???????????? spu ???????????????????????????????????????????????????????????????????????????
        AttrGroupDao baseMapper = this.baseMapper;
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }
}
