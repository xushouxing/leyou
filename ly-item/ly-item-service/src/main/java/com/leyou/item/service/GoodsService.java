package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.order.dto.CartDTO;
import com.leyou.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoodsService {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpecificationService specificationService;
    private final static Logger logger=LoggerFactory.getLogger(GoodsService.class);
    /**
     * 分页查询商品信息
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example=new Example(Spu.class);
        //模糊查询
        Example.Criteria criteria = example.createCriteria();
        PageHelper.startPage(page,rows);
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw  new LyException(ExceptionEnum.SPU_NOT_BE_FOUND);
        }
        PageInfo<Spu> pageInfo=new PageInfo<>(spus);
        ArrayList<SpuBo> spuBos=new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo=new SpuBo();
            BeanUtils.copyProperties(spu,spuBo);
            List<String> names = categoryService.queryCategoriesNamesByCids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            String cname = StringUtils.join(names, "/");
            spuBo.setCname(cname);
            String bname = brandService.queryBrandNameByBid(spu.getBrandId());
            spuBo.setBname(bname);
            spuBos.add(spuBo);
        });
        return new PageResult<>(pageInfo.getTotal(),spuBos);
    }

    /**
     * 保存商品
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //保存spu信息
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);
        //报存spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        //新增sku 
        saveSkuAndStock(spuBo);
        sendMessage(spuBo.getId(),"insert");
    }

    /**
     * 保存Sku信息和库存信息
     * @param spuBo
     */
    public void saveSkuAndStock(SpuBo spuBo){
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            //保存sku
            sku.setId(null);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spuBo.getId());
            skuMapper.insertSelective(sku);
            //保存stock
           Stock stock=new Stock();
           stock.setStock(sku.getStock());
           stock.setSkuId(sku.getId());
            stockMapper.insert(stock);
        });
    }

    /**
     * 修改商品信息
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //查出旧的sku和库存并且删除
        List<Sku> skus = specificationService.querySkuBySpuId(spuBo.getId());
        skus.forEach(sku -> {
            stockMapper.deleteByPrimaryKey(sku.getId());
            skuMapper.deleteByPrimaryKey(sku.getId());
        });
        //新增sku和库存
        saveSkuAndStock(spuBo);
        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        sendMessage(spuBo.getId(),"update");
    }

    public Spu querySpuBySpuId(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu==null){
            throw new LyException(ExceptionEnum.SPU_NOT_BE_FOUND);
        }
        return spu;
    }
    public void sendMessage(Long spuId,String routeKey){
        try {
            amqpTemplate.convertAndSend("item."+routeKey,spuId);
        } catch (AmqpException e) {
            logger.error("{}商品消息发送异常，商品id：{}", routeKey, spuId, e);
        }
    }
    @Transactional
    public void deleteStock(List<CartDTO> cartDTOs) {
        for (CartDTO cartDTO : cartDTOs) {
            stockMapper.deleteStock(cartDTO.getSkuId(),cartDTO.getNum());
        }
    }
}
