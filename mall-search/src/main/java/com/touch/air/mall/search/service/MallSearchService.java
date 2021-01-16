package com.touch.air.mall.search.service;

import com.touch.air.mall.search.vo.SearchParam;
import com.touch.air.mall.search.vo.SearchResult;

/**
 * @author: bin.wang
 * @date: 2021/1/15 13:13
 */
public interface MallSearchService {

    /**
     *返回检索结果,里面包含页面需要的所有信息
     * @param searchParam 检索的所有参数
     * @return
     */
    SearchResult search(SearchParam searchParam);
}
