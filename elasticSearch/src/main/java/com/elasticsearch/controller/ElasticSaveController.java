package com.elasticsearch.controller;

import com.common.exception.BizCodeException;
import com.common.to.es.SkuEsModel;
import com.common.utils.R;
import com.elasticsearch.service.Impl.ProductSaveServiceImpl;
import com.elasticsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/27
 **/
@Slf4j
@RequestMapping("search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    /**
     * 上架商品
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("商品上架错误{}",e);
            return R.error(BizCodeException.PRODUCT_UP_EXCEPTION.getCode(),BizCodeException.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(b) {
            return R.error(BizCodeException.PRODUCT_UP_EXCEPTION.getCode(),BizCodeException.PRODUCT_UP_EXCEPTION.getMsg());
        }else {
            return R.ok();
        }

    }

}
