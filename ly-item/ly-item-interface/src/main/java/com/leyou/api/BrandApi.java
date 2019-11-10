package com.leyou.api;
import com.leyou.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("brand")
public interface BrandApi {
    /**
     * 根据品牌的id查询品牌的名称
     * @param bid
     * @return
     */
    @GetMapping("{id}")
    String queryBrandByBid(@PathVariable("id")Long bid);

    /**
     * 根据品牌id查询品牌
     * @param bid
     * @return
     */
    @GetMapping("list/{id}")
    Brand queryBrandListByBid(@PathVariable("id")Long bid);
}
