package com.leyou.feignclient;

import com.leyou.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value ="item-service" )
public interface BrandClient extends BrandApi {
}
