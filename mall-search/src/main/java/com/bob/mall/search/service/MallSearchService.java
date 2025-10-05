package com.bob.mall.search.service;

import com.bob.mall.search.vo.SearchParam;
import com.bob.mall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
