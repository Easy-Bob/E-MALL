package com.bob.mall.product.web;

import com.bob.mall.product.service.SkuInfoService;
import com.bob.mall.product.vo.SpuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SpuItemVO itemVO = skuInfoService.item(skuId);
        model.addAttribute("item",itemVO);
        return "item";
    }
}
