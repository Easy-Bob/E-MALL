package com.bob.mall.search.service.impl;

import com.bob.common.dto.es.SkuESModel;
import com.bob.common.utils.R;
import com.bob.mall.search.config.MallElasticSearchConfiguration;
import com.bob.mall.search.constant.ESConstant;
import com.bob.mall.search.service.ElasticSearchSaveService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.Buffer;
import java.util.List;

@Service
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels) {
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);
            // set id
            indexRequest.id(skuESModel.getSkuId().toString());
            // set doc
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(skuESModel);
            indexRequest.source(json, XContentType.JSON);
            // 转换后的数据封装到Bulk
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        boolean flag = bulk.hasFailures();
        return !flag;
    }
}
