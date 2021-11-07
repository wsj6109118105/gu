package com.elasticsearch.service;

import com.elasticsearch.vo.SearchParam;
import com.elasticsearch.vo.SearchResult;

/**
 * user:lufei
 * DATE:2021/11/7
 **/
public interface MallSearchService {

    /**
     *
     * @param param 检索的所有参数
     * @return 检索的结果
     */
    SearchResult search(SearchParam param);
}
