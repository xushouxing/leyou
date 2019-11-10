package com.leyou.item.service;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.item.mapper.*;
import com.leyou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecParamMapper specParamMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        SpecGroup specGroup=new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> groups = specGroupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(groups)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FUOND);
        }
        return groups;
    }

    public List<SpecParam>querySpecParamList(Long gid,Long cid,Boolean searching,Boolean generic) {
        SpecParam specParam=new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        List<SpecParam> params = specParamMapper.select(specParam);
        if(CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.SPEC_PARAMS_NOT_FOUND);
        }
        return params;
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail==null){
            throw new LyException(ExceptionEnum.SPUDETAIL_NOT_BT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku sku=new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        skus.forEach(sku1 -> {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            sku1.setStock(stock.getStock());
        });
        return skus;
    }

    public List<SpecGroup> querySpecGrouplistByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> groups = this.querySpecGroupByCid(cid);
        groups.forEach(g -> {
            // 查询组内参数
            g.setParams(this.querySpecParamList(g.getId(), null, null, null));
        });
        return groups;
    }
}
