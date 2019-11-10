package com.leyou.common.vo;

import lombok.*;

import java.util.List;
@Data
@Setter
@Getter
public class PageResult<T> {
    private Long total; //总条数
    private List<T> items;  //当前页数据
    private Integer totalPage;  //总页数

    public PageResult(Long total, List<T> items, Integer totalPage) {
        this.total = total;
        this.items = items;
        this.totalPage = totalPage;
    }

    public PageResult() {
    }

    public PageResult(Long total, List<T> items){
        this.total=total;
        this.items=items;
    }
}
