package com.leyou.api;
import com.leyou.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("category")
public interface CategoryApi {
    /**
     * 根据分类ids查询names
     * @param ids
     * @return
     */
    @GetMapping("names")
    List<String> queryNamesByIds(@RequestParam("ids")List<Long> ids);

    /**
     * 根据分类id查询分类
     * @param ids
     * @return
     */
    @GetMapping("list/cids")
    List<Category> queryCategoriesByIds(@RequestParam("ids")List<Long> ids);
}
