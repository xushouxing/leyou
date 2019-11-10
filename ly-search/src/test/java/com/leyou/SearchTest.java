package com.leyou;

import com.leyou.common.vo.PageResult;
import com.leyou.feignclient.GoodsClient;
import com.leyou.pojo.Goods;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuBo;
import com.leyou.repository.ItemRepository;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTest {
    @Autowired
    private ItemRepository repository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Test
    public void createIndex(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }
    @Test
    public void saveGoods(){
        Integer page=1;
        Integer rows=50;
        do {
            PageResult<SpuBo> result = goodsClient.querySpuBoByPage(null, true, page, rows);
            List<Goods> goods = result.getItems().stream().map(spuBo -> {
                try {
                    return searchService.buildGoods((Spu) spuBo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            repository.saveAll(goods);
            rows=result.getItems().size();
            page++;
        }while (rows==50);
    }
}
