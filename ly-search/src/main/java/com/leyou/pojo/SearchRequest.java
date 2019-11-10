package com.leyou.pojo;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class SearchRequest {
    private Map<String,String> filter;
    private String key;  //搜索字段
    private Integer page; //当前页
    private static final Integer DEFAULT_SIZE = 20;// 每页大小，不从页面接收，而是固定大小
    private static final Integer DEFAULT_PAGE = 1;// 默认页
    public String getKey() {
        return key;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public SearchRequest(Map<String, String> filter, String key, Integer page) {
        this.filter = filter;
        this.key = key;
        this.page = page;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public Integer getPage(){
        if(page == null){
            return DEFAULT_PAGE;
        }
        // 获取页码时做一些校验，不能小于1
        return Math.max(DEFAULT_PAGE, page);
    }
    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return DEFAULT_SIZE;
    }
}
