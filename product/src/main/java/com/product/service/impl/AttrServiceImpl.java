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
        //1.保存基本数据
        this.save(entity);
        //2.保存关联关系
        if(attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(entity.getAttrId());
            relationDao.insert(relation);
        }

    }

    /**
     * 根据分类id查询对应的数据
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
            //设置分类和分组的名字
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
     * 修改是回显对应的信息数据
     * @param attrId
     * @return
     */
    @Cacheable(value = "attr",key = "'attrinfo'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        //查询详细信息
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        //分组信息,当为基本信息时才具有分组信息，否则没有不需要回显
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
        //完整路径
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
        //修改分组关联，当为基本属性时才具有分组关联信息
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
     * 根据分组id查询关联的属性
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
     * 获取分组当前没有关联的属性
     * @param groupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelation(Long groupId, Map<String, Object> params) {
        //当前分类的属性数据，而且没有被别的分组引用的属性
        //查询所属分类的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        /*List<AttrEntity> catelog_id = attrDao.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId));
        List<Long> collect = catelog_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        relationDao.selectNoExit(collect);*/
        //分类下其他分组.ne("attr_group_id", groupId)
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //其他分组所关联的属性
        List<AttrAttrgroupRelationEntity> attr_group_id = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attr_id = attr_group_id.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //查询出去以上属性剩下的属性
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
     * 在所有属性里面挑出指定的检索属性
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> SelectSearchAttrs(List<Long> attrIds) {

        return baseMapper.SelectSearchAttrs(attrIds);
    }

}
