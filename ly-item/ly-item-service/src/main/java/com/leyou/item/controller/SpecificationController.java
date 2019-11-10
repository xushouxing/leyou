package com.leyou.item.controller;

import com.leyou.item.service.SpecificationService;
import com.leyou.pojo.Sku;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import com.leyou.pojo.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;
    @GetMapping("spec/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid")Long cid){
     return  ResponseEntity.ok(specificationService.querySpecGroupByCid(cid));
    }
    @GetMapping("spec/params")
    public ResponseEntity<List<SpecParam>> querySpecParamList(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching,
            @RequestParam(value = "generic",required =false)Boolean generic
    ){
       return ResponseEntity.ok(specificationService.querySpecParamList(gid,cid,searching,generic));
    }
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
       SpuDetail spuDetail=specificationService.querySpuDetailBySpuId(spuId);
       return ResponseEntity.ok(spuDetail);
    }
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long spuId){
        List<Sku> skus=specificationService.querySkuBySpuId(spuId);
        return ResponseEntity.ok(skus);
    }
    @GetMapping("/spec/group/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGrouplistByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> specGroups = specificationService.querySpecGrouplistByCid(cid);
        return ResponseEntity.ok(specGroups);
    }
}
