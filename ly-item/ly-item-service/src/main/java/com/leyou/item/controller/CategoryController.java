package com.leyou.item.controller;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.item.service.CategoryService;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam(value = "pid",required = false,defaultValue = "0")Long pid){
        if(pid==null || pid.longValue()<0){
            throw new LyException(ExceptionEnum.PRICE_NOT_BE_NULL);
        }
        List<Category> categories = categoryService.queryCategoryByPid(pid);
        if(CollectionUtils.isEmpty(categories)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_BE_FOUND);
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据分类ids查询names
     * @param ids
     * @return
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids")List<Long> ids){
        List<String> strings = categoryService.queryCategoriesNamesByCids(ids);
        return ResponseEntity.ok(strings);
    }
    @GetMapping("list/cids")
    public ResponseEntity<List<Category>> queryCategoriesByIds(@RequestParam("ids")List<Long> ids){
        List<Category> categories = categoryService.queryCategoriesByCids(ids);
        return ResponseEntity.ok(categories);
    }
}
