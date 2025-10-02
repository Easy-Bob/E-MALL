package com.bob.mall.search.service;

import com.bob.common.dto.es.SkuESModel;
import com.bob.common.utils.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchSaveService {
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;

}
