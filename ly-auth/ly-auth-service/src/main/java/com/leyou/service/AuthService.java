package com.leyou.service;

import com.leyou.User;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.config.JwtProperties;
import com.leyou.feignclient.UserClient;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties properties;
    public String authentication(String username, String password) {
        try {
        //校验用户名密码
        User user = userClient.query(username, password);
        //生成jwt
        String token = null;
            token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                    properties.getPrivateKey(), properties.getExpire());
            if (StringUtils.isBlank(token)) {
               throw new LyException(ExceptionEnum.TOKEN_FAIL);
            }
           return token;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.TOKEN_FAIL);
        }
    }
}
