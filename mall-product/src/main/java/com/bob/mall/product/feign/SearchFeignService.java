package com.bob.mall.product.feign;

import com.bob.common.dto.SkuReductionDTO;
import com.bob.common.dto.SpuBoundsDTO;
import com.bob.common.dto.es.SkuESModel;
import com.bob.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels);
}
