package com.elasticsearch.service;

import com.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/27
 **/
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
