package com.leyou.feignclient;

import com.leyou.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient("ly-user-service")
public interface UserClient extends UserApi {

}
