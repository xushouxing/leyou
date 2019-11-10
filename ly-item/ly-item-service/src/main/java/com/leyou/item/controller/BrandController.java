package com.leyou.item.controller;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.vo.PageResult;
import com.leyou.item.service.BrandService;
import com.leyou.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌
     * @param key
     * @param rows
     * @param sortBy
     * @param page
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "desc",required = false)Boolean desc
    ){
        PageResult<Brand> result = brandService.queryBrandsByPage(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增品牌
     * @param cids
     * @param brand
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(@RequestParam("cids")List<Long> cids,Brand brand){
        this.brandService.saveBrand(cids,brand);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(brandService.queryBrandsByCid(cid));
    }

    /**
     * 根据品牌的id查询品牌的名称
     * @param bid
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<String> queryBrandByBid(@PathVariable("id")Long bid){
        return ResponseEntity.ok(brandService.queryBrandNameByBid(bid));
    }
    @GetMapping("list/{id}")
    public ResponseEntity<Brand> queryBrandListByBid(@PathVariable("id")Long bid){
        return ResponseEntity.ok(brandService.queryBrandListByBid(bid));
    }
}
