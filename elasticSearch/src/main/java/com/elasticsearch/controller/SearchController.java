package com.elasticsearch.controller;

import com.elasticsearch.service.MallSearchService;
import com.elasticsearch.vo.SearchParam;
import com.elasticsearch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * user:lufei
 * DATE:2021/11/6
 **/
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {

        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        SearchResult result = null;
        result = mallSearchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }
}
