package com.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.constant.ProductConstant;
import com.product.dao.AttrAttrgroupRelationDao;
import com.product.dao.AttrGroupDao;
import com.product.dao.CategoryDao;
import com.product.entity.AttrAttrgroupRelationEntity;
import com.product.entity.AttrGroupEntity;
import com.product.entity.CategoryEntity;
import com.product.service.CategoryService;
import com.product.vo.AttrRespVo;
import com.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.product.dao.AttrDao;
import com.product.entity.AttrEntity;
import com.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        //1.??????????????????
        this.save(entity);
        //2.??????????????????
        if(attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(entity.getAttrId());
            relationDao.insert(relation);
        }

    }

    /**
     * ????????????id?????????????????????
     * @param params
     * @param catelogId
     * @param attrType
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if(catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
        }

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(obj->{
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> attr_id = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //??????????????????????????????
            if("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity id = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
                if (id != null && id.getAttrGroupId()!=null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(id.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attr_id);
        return pageUtils;
    }

    /**
     * ????????????????????????????????????
     * @param attrId
     * @return
     */
    @Cacheable(value = "attr",key = "'attrinfo'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        //??????????????????
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        //????????????,????????????????????????????????????????????????????????????????????????
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attr_id = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
            if(attr_id!=null){
                Long attrGroupId = attr_id.getAttrGroupId();
                attrRespVo.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                if(attrGroupEntity!=null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //????????????
        Long catelogId = attrRespVo.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if(categoryEntity!=null){
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.update(attrEntity,new UpdateWrapper<AttrEntity>().eq("attr_id",attrEntity.getAttrId()));
        //?????????????????????????????????????????????????????????????????????
        if(attr.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            Long attrGroupId = attr.getAttrGroupId();
            Long attrId = attr.getAttrId();

            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrId(attrId);
            relation.setAttrGroupId(attrGroupId);
            Long selectCount = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(selectCount>0){
                relationDao.update(relation,new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrId));
            }else {
                relationDao.insert(relation);
            }
        }

    }

    /**
     * ????????????id?????????????????????
     * @param groupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long groupId) {
        List<AttrAttrgroupRelationEntity> attr_group_id = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", groupId));

        List<Long> attrId = attr_group_id.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        if(attrId == null || attrId.size()==0){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrId);

        return attrEntities;
    }

    /**
     * ???????????????????????????????????????
     * @param groupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelation(Long groupId, Map<String, Object> params) {
        //????????????????????????????????????????????????????????????????????????
        //???????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        /*List<AttrEntity> catelog_id = attrDao.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId));
        List<Long> collect = catelog_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        relationDao.selectNoExit(collect);*/
        //?????????????????????.ne("attr_group_id", groupId)
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //??????????????????????????????
        List<AttrAttrgroupRelationEntity> attr_group_id = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attr_id = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //???????????????????????????????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attr_id!=null&&attr_id.size()>0){
            wrapper.notIn("attr_id", attr_id);
        }
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * ????????????????????????????????????????????????
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> SelectSearchAttrs(List<Long> attrIds) {

        return baseMapper.SelectSearchAttrs(attrIds);
    }

}
