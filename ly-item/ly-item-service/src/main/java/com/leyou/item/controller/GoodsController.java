package com.leyou.item.controller;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.service.GoodsService;
import com.leyou.order.dto.CartDTO;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SkuMapper skuMapper;
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    ){
        return ResponseEntity.ok(goodsService. querySpuBoByPage(key,saleable,page,rows));
    }

    /**
     * 保存商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("spu/spuId")
    public ResponseEntity<Spu> querySpuBySpuId(@RequestParam("spuId")Long spuId){
        return ResponseEntity.ok(goodsService.querySpuBySpuId(spuId));
    }
    /**
     * 根据skuid查询sku
     * @param ids
     * @return
     */
    @GetMapping("skus")
    public ResponseEntity<List<Sku>> querySkuBySkuId(@RequestParam("ids") List<Long> ids){
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new  RuntimeException();
        }
        return ResponseEntity.ok(skus);
    }
    @PostMapping("stock/del")
    public ResponseEntity<Void> deleteStock(@RequestBody List<CartDTO> cartDTOS){
        goodsService.deleteStock(cartDTOS);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
