package com.touch.air.mall.product.web;

import com.touch.air.mall.product.entity.CategoryEntity;
import com.touch.air.mall.product.service.CategoryService;
import com.touch.air.mall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: bin.wang
 * @date: 2021/1/7 16:51
 */
@Controller
public class IndexController {
    @Resource
    private CategoryService categoryService;


    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        //TODO 查出所有的一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getFirstLevelCategroys();
        //视图解析器进行拼串
        // classpath:/templates/  +返回值+  .html
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){
        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

}

