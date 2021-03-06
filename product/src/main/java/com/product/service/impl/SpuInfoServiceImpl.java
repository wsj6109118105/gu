package com.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.common.constant.ProductConstant;
import com.common.to.SkuReductionTo;
import com.common.to.SpuBoundsTo;
import com.common.to.es.SkuEsModel;
import com.common.to.es.SkuHasStockVo;
import com.common.utils.R;
import com.product.entity.*;
import com.product.feign.SearchFeignService;
import com.product.feign.WareFeignService;
import com.product.feign.couponFeignService;
import com.product.service.*;
import com.product.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageTranscoder;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    couponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ?????????????????????
     * @param vo
     */
    @GlobalTransactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.??????????????????  pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        //2.?????? spu ???????????????    pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveDescInfo(descEntity);
        //3.?????? spu ????????????    pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.?????? spu ????????????    pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((obj) -> {
            ProductAttrValueEntity ValueEntity = new ProductAttrValueEntity();
            ValueEntity.setSpuId(spuInfoEntity.getId());
            ValueEntity.setAttrId(obj.getAttrId());
            AttrEntity byId = attrService.getById(obj.getAttrId());
            ValueEntity.setAttrName(byId.getAttrName());
            ValueEntity.setAttrValue(obj.getAttrValues());
            ValueEntity.setQuickShow(obj.getShowDesc());
            return ValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);
        //5.?????? spu ????????????    gu_sms---->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds,spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if(r.getCode()!=0){
            log.error("????????????spu??????????????????");
        }
        //6.?????? spu ????????? sku ??????
        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size()>0){
            skus.forEach(item->{
                //  1 ) sku????????????    pms_sku_info
                String defaultImage = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg()==1){
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                //  2 ) sku????????????    pms_sku_images
                List<SkuImagesEntity> collect1 = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //??????true ??????
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //TODO ?????????????????????????????????
                skuImagesService.saveBatch(collect1);
                //  3 ) sku??????????????????    pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                if(attr!=null&&attr.size()>0){
                    List<SkuSaleAttrValueEntity> collect2 = attr.stream().map(a -> {
                        SkuSaleAttrValueEntity ValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(a, ValueEntity);
                        ValueEntity.setSkuId(skuId);
                        return ValueEntity;
                    }).collect(Collectors.toList());
                    skuSaleAttrValueService.saveBatch(collect2);
                }
                //  4 ) sku????????????????????????
                //  gu_sms---->sms_sku_ladder(?????????)---->sms_sku_full_reduction(?????????)---->sms_member_price(???????????????)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullPrice().compareTo(new BigDecimal(0))==1&&skuReductionTo.getFullCount()>0){
                    R r1 = couponFeignService.saveReduction(skuReductionTo);
                    if(r1.getCode()!=0){
                        log.error("????????????sku??????????????????");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key).or().like("spu_description",key);
            });
        }

        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("catalog_id",catelogId);
        }
        /**
         * status: 0
         * key: zz
         * brandId: 4
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //?????????????????????
        //1.spu ?????? sku ?????????????????????????????????
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
        //  ???????????? sku ??????????????????????????????????????????
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> AttrIds = productAttrValueEntities.stream().map(x -> {
            return x.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrId = attrService.SelectSearchAttrs(AttrIds);
        Set<Long> idSet = new HashSet<>(searchAttrId);
        List<SkuEsModel.Attrs> attrs = productAttrValueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());
        //  ??????????????????????????????????????????
        List<Long> collect = skus.stream().map(sku -> {
            return sku.getSkuId();
        }).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getHasStock(collect);
            List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>(){});
            //  ??????????????????
            stockMap = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e) {
            log.error("????????????????????????{}"+e);
        }



        //2.?????? sku ??????
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> uoProduct = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,skuEsModel);
            //skuPrice
            skuEsModel.setSkuPrice(sku.getPrice());
            //skuImg
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //hasStock
            if(finalStockMap ==null){
                skuEsModel.setHasStock(true);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //hotScore
            // TODO ???????????? 0???
            skuEsModel.setHotScore(0L);
            //  ????????????????????????????????????
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            //brandName
            skuEsModel.setBrandName(brand.getName());
            //brandImg
            skuEsModel.setBrandImg(brand.getLogo());
            //catalogName
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            //??????????????????
            skuEsModel.setAttrs(attrs);

            return skuEsModel;
        }).collect(Collectors.toList());

        // ?????? ES ????????????
        R r = searchFeignService.productStatusUp(uoProduct);
        if(r.getCode()==0){
            // success
            // todo ???????????????????????????  pms_spu_info
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }else {
            // fail
            // todo ?????????????????????????????????????????????
            //

        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity entity = getById(spuId);
        return entity;
    }


}
