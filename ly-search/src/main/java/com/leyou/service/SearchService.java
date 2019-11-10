package com.leyou.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.feignclient.BrandClient;
import com.leyou.feignclient.CategoryClient;
import com.leyou.feignclient.GoodsClient;
import com.leyou.feignclient.SpecificationClient;
import com.leyou.pojo.*;
import com.leyou.repository.ItemRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private GoodsClient goodsClient;

    /**
     * 构建goods
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        Goods goods=new Goods();
        //设置id
        goods.setId(spu.getId());
        //卖点
        goods.setSubTitle(spu.getSubTitle());
        //cid1 cid2 cid3
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        //创建时间
        goods.setCreateTime(spu.getCreateTime());
        //设置brandId
        goods.setBrandId(spu.getBrandId());
        //设置all字段
        List<String> categoriesName = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        String brandname = brandClient.queryBrandByBid(spu.getBrandId());
        goods.setAll(spu.getTitle()+brandname+StringUtils.join(categoriesName," "));
       //设置价格
        List<Sku> skus = specificationClient.querySkuBySpuId(spu.getId());
        List<Long> prices = skus.stream().map(sku -> {
            return sku.getPrice();
        }).collect(Collectors.toList());
        goods.setPrice(prices);
        //设置skus
        List<Map<String,Object>> skuMapList=new ArrayList<>();
        skus.forEach(sku -> {
            Map<String,Object> skuMap=new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image",StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(),",")[0]: "");
            skuMapList.add(skuMap);
        });
        goods.setSkus(JsonUtils.serialize(skuMapList));
        //设置specs
        //(1 查询可搜索的参数集合
        List<SpecParam> specParams = specificationClient.querySpecParamList(null, spu.getCid3(), true,null);
        // （2查询spuDetail
        SpuDetail spuDetail = specificationClient.querySpuDetailBySpuId(spu.getId());
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long, Object> genericSpecMap = JsonUtils.parseMap(genericSpec, Long.TYPE, Object.class);
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<Object>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<Object>>>() {
        });
        Map<String,Object> specs=new HashMap<>();
        specParams.forEach(specParam -> {
            if(specParam.getGeneric()){
                Object value = genericSpecMap.get(specParam.getId());
                if (specParam.getNumeric()){
                    value=chooseSegment(value.toString(),specParam);
                }
                specs.put(specParam.getName(),value);
            }else {
                List<Object> list = specialSpecMap.get(specParam.getId());
                specs.put(specParam.getName(),list);
            }
        });
        //设置specs
        goods.setSpecs(specs);
        return goods;
    }
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        //获取搜索字段
        String key = request.getKey();
        NativeSearchQueryBuilder searchQuery=new NativeSearchQueryBuilder();
        if(StringUtils.isBlank(key)){
            return null;
        }
        BoolQueryBuilder queryBuilder =buildQueryResult(request);
        searchQuery.withQuery(queryBuilder);
        //过滤搜索结果
        searchQuery.withSourceFilter(new FetchSourceFilter(new String[]{
                "id","skus","subTitle"
        },null));
        //添加分页
        searchQuery.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加聚合  分类聚合
        String categroyAggName="category_agg";
        searchQuery.addAggregation(AggregationBuilders.terms(categroyAggName).field("cid3"));
        //品牌聚合
        String brandAggName="brand_agg";
        searchQuery.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        AggregatedPage<Goods> goodsPage=(AggregatedPage)itemRepository.search(searchQuery.build());
        //解析分类聚合
        List<Map<String,Object>> categories=getCategoryiesAgg(goodsPage.getAggregation(categroyAggName));
        //解析品牌聚合
        List<Brand> brands=getBrandsAgg(goodsPage.getAggregation(brandAggName));
        List<Map<String,Object>> specs=null;
        //查询分类是否唯一 ，唯一这
        if(categories!=null && categories.size()==1){
              specs=getParamAggResult((Long) categories.get(0).get("id"),queryBuilder);
        }
        //分装结果
        return new SearchResult(goodsPage.getTotalElements(),goodsPage.getContent(),goodsPage.getTotalPages(),categories,brands,specs);
    }

    /**
     * 查询条件
     * @param request
     * @return
     */
    private BoolQueryBuilder buildQueryResult(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        if(CollectionUtils.isEmpty(request.getFilter())){
             return boolQueryBuilder;
         }
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            // 如果过滤条件是“品牌”, 过滤的字段名：brandId
            if (StringUtils.equals("品牌", key)) {
                key = "brandId";
            } else if (StringUtils.equals("分类", key)) {
                // 如果是“分类”，过滤字段名：cid3
                key = "cid3";
            } else {
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs." + key + ".keyword";
            }
            String value = entry.getValue();
            TermQueryBuilder termQuery = QueryBuilders.termQuery(key, value);
            boolQueryBuilder.filter().add(termQuery);
        }
        return boolQueryBuilder;
    }

    /**
     * 参数聚合结果
     * @param id
     * @param queryBuilder
     * @return
     */
        private List<Map<String,Object>> getParamAggResult(Long id, QueryBuilder queryBuilder) {
        NativeSearchQueryBuilder searchQueryBuilder=new NativeSearchQueryBuilder();
        //添加基本查询结果
        searchQueryBuilder.withQuery(queryBuilder);
        //查询可搜索的参数
        List<SpecParam> specParams = specificationClient.querySpecParamList(null, id, true,null);
        //添加聚合
        specParams.forEach(specParam -> {
            searchQueryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword"));
        });
        //结果过滤
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //解析结果
        AggregatedPage<Goods> goods = (AggregatedPage<Goods>) itemRepository.search(searchQueryBuilder.build());
        Aggregations aggregations = goods.getAggregations();
        List<Map<String,Object>> specs=new ArrayList<>();
        specParams.forEach(specParam -> {
            Map<String,Object> map=new HashMap<>();
            StringTerms terms = (StringTerms) aggregations.get(specParam.getName());
            List<StringTerms.Bucket> buckets = terms.getBuckets();
            ArrayList<Object> arrayList=new ArrayList<>();
            buckets.forEach(bucket -> {
                arrayList.add(bucket.getKeyAsString());
            });
            map.put("k",specParam.getName());
            map.put("options",arrayList);
            specs.add(map);
        });
        return specs;
    }

    /**
     * 分类聚合结果
     * @param aggregation
     * @return
     */
    private List<Map<String,Object>> getCategoryiesAgg(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        ArrayList<Long> cids=new ArrayList<>();
        buckets.forEach(bucket -> {
            long value = bucket.getKeyAsNumber().longValue();
            cids.add(value);
        });
        List<Category> categories = categoryClient.queryCategoriesByIds(cids);
        List<Map<String,Object>> categoryListMap=new ArrayList<>();
        categories.forEach(category -> {
            Map<String,Object> map=new HashMap<>();
            map.put("id",category.getId());
            map.put("name",category.getName());
            categoryListMap.add(map);
        });
        return categoryListMap;
    }

    /**
     * 品牌聚合结果
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandsAgg(Aggregation aggregation){
        LongTerms terms = (LongTerms) aggregation;
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        ArrayList<Brand> brands=new ArrayList<>();
        buckets.forEach(bucket -> {
            Brand brand=new Brand();
            brand = brandClient.queryBrandListByBid(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
        return brands;
    }

    /**
     * 保存商品数据到索引库
     * @param id
     * @throws IOException
     */
    public void createIndex(Long id) throws IOException {

        Spu spu = this.goodsClient.querySpuBySpuId(id);
        // 构建商品
        Goods goods = this.buildGoods(spu);

        // 保存数据到索引库
        this.itemRepository.save(goods);
    }
}
