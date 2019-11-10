package com.leyou.api;

import com.leyou.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
public interface UserApi {
    /**
     * 校验用户账号密码
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    User query(@RequestParam("username") String username,
               @RequestParam("password") String password);
}
