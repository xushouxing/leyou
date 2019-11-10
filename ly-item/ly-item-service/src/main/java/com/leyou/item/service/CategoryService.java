package com.leyou.item.service;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.pojo.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    public List<Category> queryCategoryByPid(Long pid){
        Category category=new Category();
        category.setParentId(pid);
        List<Category> categories = categoryMapper.select(category);
        return categories;
    }
    public List<String> queryCategoriesNamesByCids(List<Long> cids){
        List<Category> categories = categoryMapper.selectByIdList(cids);
        List<String> names = categories.stream().map(category -> {
            return category.getName();
        }).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(names)){
           throw new RuntimeException();
        }
        return names;
    }

    public List<Category> queryCategoriesByCids(List<Long> ids) {
        List<Category> categories = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORIES_FOUNDBYCIDS_FALIS);
        }
        return categories;
    }
}
