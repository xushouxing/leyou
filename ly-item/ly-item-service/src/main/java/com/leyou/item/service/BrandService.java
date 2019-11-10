package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows
            , String sortBy, Boolean desc
    ) {
        Example example = new Example(Brand.class);
        //分页
        PageHelper.startPage(page, rows);
        if (StringUtils.isNotBlank(key)) {
            //过滤条件
            Example.Criteria criteria = example.createCriteria();
            //模糊查询
            criteria.orLike("name", "%" + key + "%").orEqualTo("letter", key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)) {
            //排序
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        if(org.springframework.util.CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_BE_FOUND);
        }
        //包装pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        PageResult<Brand> result = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return result;
    }
    @Transactional
    public void saveBrand(List<Long> cids, Brand brand) {
        int i = brandMapper.insert(brand);
        if(i!=1){
            throw new LyException(ExceptionEnum.BRAND_INSERT_FAIL);
        }
        cids.forEach(cid ->{
            brandMapper.insertBrandAndCategory(cid,brand.getId());
        });
    }

    /**
     * 根据品牌id查询品牌名称
     * @param id
     * @return
     */
    public String queryBrandNameByBid(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null){
            throw new RuntimeException();
        }
        return brand.getName();
    };

    public List<Brand> queryBrandsByCid(Long cid) {
        List<Long> bids = brandMapper.queryCategoryIdByBrandId(cid);
        List<Brand> brands = brandMapper.selectByIdList(bids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new RuntimeException();
        }
        return brands;
    }

    public Brand queryBrandListByBid(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }
}
