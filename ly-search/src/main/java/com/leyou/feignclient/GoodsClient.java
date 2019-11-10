package com.leyou.feignclient;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value ="item-service" )
public interface GoodsClient extends GoodsApi {
}
