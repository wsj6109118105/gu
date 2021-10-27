package com.elasticsearch.service.Impl;

import com.alibaba.fastjson.JSON;
import com.common.to.es.SkuEsModel;
import com.elasticsearch.config.ElasticConfig;
import com.elasticsearch.constant.EsConstant;
import com.elasticsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * user:lufei
 * DATE:2021/10/27
 **/
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 上架商品
     * @param skuEsModels
     */
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        //保存到es
        BulkRequest bulkRequest = new BulkRequest();
        //构造保存请求
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        // todo 错误处理
        if(b){
            List<String> collect = Arrays.stream(bulk.getItems()).map(x -> {
                return x.getId();
            }).collect(Collectors.toList());
            log.error("商品上架出错：{}",collect);
        }
        return b;

    }
}
