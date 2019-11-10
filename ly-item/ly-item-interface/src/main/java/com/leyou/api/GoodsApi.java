package com.leyou.api;
import com.leyou.common.vo.PageResult;
import com.leyou.order.dto.CartDTO;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuBo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    /**
     * 分页查询spuBo
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<SpuBo> querySpuBoByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    );

    /**
     * 根据spuId查询spu
     * @param spuId
     * @return
     */
    @GetMapping("spu/spuId")
    Spu querySpuBySpuId(@RequestParam("spuId")Long spuId);

    /**
     * 根据skuid查询sku
     * @param ids
     * @return
     */
    @GetMapping("skus")
     List<Sku> querySkuBySkuId(@RequestParam("ids") List<Long> ids);

    /**
     * 减库存
     * @param cartDTOS
     */
    @PostMapping("stock/del")
    void deleteStock(@RequestBody List<CartDTO> cartDTOS);
}
