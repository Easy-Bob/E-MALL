package com.bob.mall.product.feign;

import com.bob.common.dto.SkuHasStockDto;
import com.bob.common.dto.SkuReductionDTO;
import com.bob.common.dto.SpuBoundsDTO;
import com.bob.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-ware")
public interface WareSkuFeignService {

    @PostMapping("/ware/waresku/hasStock")
    List<SkuHasStockDto> getSkusHasStock(List<Long> skuIds);
}
