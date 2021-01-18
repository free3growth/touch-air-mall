package com.touch.air.mall.search.controller;

import com.touch.air.mall.search.service.MallSearchService;
import com.touch.air.mall.search.vo.SearchParam;
import com.touch.air.mall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: bin.wang
 * @date: 2021/1/15 11:17
 */
@Controller
public class SearchController {

    @Resource
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest httpServletRequest) {

        searchParam.set_url(httpServletRequest.getQueryString());
        //1、根据传递来的页面参数查询，去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        //2、返回数据 交由页面渲染
        model.addAttribute("result", result);
        return "list";
    }

}
