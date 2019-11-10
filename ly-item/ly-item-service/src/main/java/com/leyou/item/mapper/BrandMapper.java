package com.leyou.item.mapper;

import com.leyou.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>,SelectByIdListMapper<Brand,Long> {
    @Insert("insert into tb_category_brand(category_id,brand_id) values(#{cid},#{bid})")
    int insertBrandAndCategory(@Param("cid") Long cid,@Param("bid") Long bid);
    @Select("select brand_id from tb_category_brand where category_id=#{cid}")
    List<Long> queryCategoryIdByBrandId(@Param("cid")Long cid);
}
