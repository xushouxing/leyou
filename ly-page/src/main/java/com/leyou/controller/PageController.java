package com.leyou.controller;

import com.leyou.service.GoodsHtmlService;
import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {
    @Autowired
    private GoodsHtmlService goodsHtmlService;
    @Autowired
    private PageService pageService;
    @GetMapping("{id}.html")
    public String showPage(@PathVariable("id")Long id, Model model){
        Map<String, Object> modelMap=pageService.loadData(id);
        model.addAllAttributes(modelMap);
      /*  // 页面静态化
        this.goodsHtmlService.asyncExcute(id);*/
        return "item";
    }
}
