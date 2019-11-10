package com.leyou.feignclient;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value ="item-service" )
public interface CategoryClient extends CategoryApi {
}
