package com.bob.mall.search.controller;

import com.bob.common.dto.es.SkuESModel;
import com.bob.common.exception.BizCodeEnume;
import com.bob.common.utils.R;
import com.bob.mall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/search/save")
@RestController
@Slf4j
public class ElasticSearchSaveController {

    @Autowired
    private ElasticSearchSaveService elasticSearchSaveService;
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels){
        Boolean b = false;
        try {
            b = elasticSearchSaveService.productStatusUp(skuESModels);
        } catch (IOException e) {
            log.error("ElasticSearch商品商家错误,{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(b){
            return R.ok();
        }
        return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
    }
}
