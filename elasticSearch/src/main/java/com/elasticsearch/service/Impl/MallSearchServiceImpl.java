package com.elasticsearch.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.to.es.SkuEsModel;
import com.common.utils.R;
import com.elasticsearch.constant.EsConstant;
import com.elasticsearch.feign.ProductFeignService;
import com.elasticsearch.service.MallSearchService;
import com.elasticsearch.vo.AttrResponseVo;
import com.elasticsearch.vo.SearchParam;
import com.elasticsearch.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * user:lufei
 * DATE:2021/11/7
 **/
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;
    //private NestedAggregationBuilder attrs;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = null;
        // 构建出 DSL 语句

        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            // 执行请求
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            // 分析响应数据，封装成指定的格式
            result = buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建结果数据
     * @param
     * @return 返回结果数据
     */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        // 返回所有查询到的商品
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits!=null&&hitsHits.length>0) {
            for (SearchHit hitsHit : hitsHits) {
                String sourceAsString = hitsHit.getSourceAsString();
                SkuEsModel skuEsModel = JSONObject.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hitsHit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                skuEsModels.add(skuEsModel);
            }
        }
        result.setProduct(skuEsModels);
        // 当前商品涉及到的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId((Long) bucket.getKey());
            String attr_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name_agg);
            List<String> list = new ArrayList<>();
            for (Terms.Bucket attr_value_agg : ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets()) {
                list.add(attr_value_agg.getKeyAsString());
            }
            attrVo.setAttrValue(list);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        // 当前商品涉及到的所有品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> brand_buckets = brand_agg.getBuckets();
        for (Terms.Bucket brand_bucket : brand_buckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 品牌id
            brandVo.setBrandId((Long) brand_bucket.getKey());
            // 品牌名字
            ParsedStringTerms brand_name_agg = brand_bucket.getAggregations().get("brand_name_agg");
            String keyAsString = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(keyAsString);
            // 品牌图片
            ParsedStringTerms brand_img_agg = brand_bucket.getAggregations().get("brand_img_agg");
            String keyAsString1 = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(keyAsString1);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        // 当前商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId((Long) bucket.getKey());
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String keyAsString = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(keyAsString);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        // =============以上从聚合信息中获取=================
        // 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 分页页码
        result.setPageNum(param.getPageNum());
        // 分页总页码
        int totalPages = (int) (total%EsConstant.PRODUCT_PageSize == 0?total/EsConstant.PRODUCT_PageSize:(total/EsConstant.PRODUCT_PageSize+1));
        result.setTotalPages(totalPages);
        List<Integer> list = new ArrayList<>();
        for (int i = 1;i<totalPages;i++) {
            list.add(i);
        }
        result.setPageNavs(list);
        // 构建面包屑导航
        if (param.getAttrs()!=null && param.getAttrs().size()>0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.valueOf(s[0]));
                if (r.getCode()==0) {
                    AttrResponseVo attr1 = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attr1.getAttrName());
                }else {
                    navVo.setNavName(s[0]);
                }
                // 取消掉面包屑之后，要跳转到哪个地方，将请求地址的url里面的当前值置空
                attr = attr.replace(" ","%20");   // 处理浏览器对空格的编码
                String replace = param.get_queryString().replace("&attrs=" + attr, "");
                navVo.setLink("http://search.happymall.mall/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(navVos);
        }
        return result;
    }

    /**
     * 准备检索请求
     * @return 返回检索请求
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder();    //构建 DSL 语句
        /**
         * 模糊匹配，过滤
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        if (param.getCatalog3Id()!=null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        if (param.getBrandId()!=null && param.getBrandId().size()>0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        if (param.getAttrs()!=null&&param.getAttrs().size()>0) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];     // 属性id
                String[] attrValue = s[1].split(":");   //属性值
                boolQuery1.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery1.must(QueryBuilders.termsQuery("attrs.attrValue",attrValue));
                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", boolQuery1, ScoreMode.None);
                boolQuery.filter(attrs);
            }
        }

        if (param.getHasStock()!=null) {
            boolQuery.filter(QueryBuilders.termsQuery("hasStock", param.getHasStock() == 1));
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                skuPrice.gte(s[0]);
                skuPrice.lte(s[1]);
            }else if (s.length ==1){
                if (param.getSkuPrice().startsWith("_")) {
                    skuPrice.lte(s[0]);
                }else if (param.getSkuPrice().endsWith("_")) {
                    skuPrice.gte(s[0]);
                }
            }
            boolQuery.filter(skuPrice);
        }

        builder.query(boolQuery);

        // 排序，分页，高亮
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            builder.sort(s[0],s[1].equalsIgnoreCase("desc")?SortOrder.DESC:SortOrder.ASC);
        }

        builder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PageSize);
        builder.size(EsConstant.PRODUCT_PageSize);

        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }
        //聚合分析
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId");
        brand_agg.size(10);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brand_agg);
        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(10);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        builder.aggregation(catalog_agg);
        // 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attr_agg.subAggregation(attr_id_agg);
        builder.aggregation(attr_agg);

        String s = builder.toString();
        System.out.println("构建的 DSL 语句"+s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
        return searchRequest;
    }
}
