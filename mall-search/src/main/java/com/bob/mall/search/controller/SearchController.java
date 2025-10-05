package com.bob.mall.search.controller;

import com.bob.mall.search.service.MallSearchService;
import com.bob.mall.search.vo.SearchParam;
import com.bob.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping(value = {"/list.html", "/", "index.html"})
    public String listPage(SearchParam searchParam, Model model){
        SearchResult search = mallSearchService.search(searchParam);
        model.addAttribute("result", search);
        return "index";
    }


}
