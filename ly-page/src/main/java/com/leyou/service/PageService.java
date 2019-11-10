package com.leyou.service;

import com.leyou.feignclient.BrandClient;
import com.leyou.feignclient.CategoryClient;
import com.leyou.feignclient.GoodsClient;
import com.leyou.feignclient.SpecificationClient;
import com.leyou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    /**
     * 商品详细页面数据封装
     * @param id
     * @return
     */
    public Map<String, Object> loadData(Long id) {
        //spu
        Spu spu = goodsClient.querySpuBySpuId(id);
        //skus
        List<Sku> skus = specificationClient.querySkuBySpuId(id);
        //spudetail
        SpuDetail spuDetail = specificationClient.querySpuDetailBySpuId(id);
        // 查询分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", cids.get(i));
            categoryMap.put("name", names.get(i));
            categories.add(categoryMap);
        }
        //brand
        Brand brand = brandClient.queryBrandListByBid(spu.getBrandId());
        //groups
        List<SpecGroup> specGroups = specificationClient.querySpecGrouplistByCid(spu.getCid3());
        //paramMap
        List<SpecParam> specParams = specificationClient.querySpecParamList(null, spu.getCid3(), null, false);
        Map<Long, String> paramMap = new HashMap<>();
        specParams.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });
        Map<String, Object> map = new HashMap<>();
        // 封装spu
        map.put("spu", spu);
        // 封装spuDetail
        map.put("spuDetail", spuDetail);
        // 封装sku集合
        map.put("skus", skus);
        // 分类
        map.put("categories", categories);
        // 品牌
        map.put("brand", brand);
        // 规格参数组
        map.put("groups", specGroups);
        // 查询特殊规格参数
        map.put("paramMap", paramMap);
        return map;
    }
}
