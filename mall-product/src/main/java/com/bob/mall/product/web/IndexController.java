package com.bob.mall.product.web;

import com.bob.mall.product.entity.CategoryEntity;
import com.bob.mall.product.service.CategoryService;
import com.bob.mall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/","/index.html","/home","/home.html"})
    public String index(Model model) {
        List<CategoryEntity> list = categoryService.getLevelCategory();
        model.addAttribute("categorys", list);
        return "index";  // templates/index.html
    }


    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalog2JSON(){
        Map<String, List<Catalog2VO>> map = categoryService.getCatelog2JSON();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String Hello(){
        return "hello";
    }

}