package com.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

        AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
        relation.setAttrGroupId(attr.getAttrGroupId());
        relation.setAttrId(entity.getAttrId());
        relationDao.insert(relation);

    }

    /**
     * 根据分类id查询对应的数据
     * @param params
     * @param catelogId
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

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
            AttrAttrgroupRelationEntity id = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
            if (id != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(id.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            //设置分类和分组的名字
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
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        //查询详细信息
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrRespVo);

        //分组信息
        AttrAttrgroupRelationEntity attr_id = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
        if(attr_id!=null){
            Long attrGroupId = attr_id.getAttrGroupId();
            attrRespVo.setAttrGroupId(attrGroupId);
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
            if(attrGroupEntity!=null) {
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
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
