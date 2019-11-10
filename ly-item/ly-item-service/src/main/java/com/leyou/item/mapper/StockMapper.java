package com.leyou.item.mapper;

import com.leyou.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface StockMapper extends Mapper<Stock> {
    @Update("UPDATE tb_stock SET stock=stock-#{num} where sku_id=#{id} and stock>=#{num}")
    int deleteStock(@Param("id")Long id, @Param("num")Integer num);
}
