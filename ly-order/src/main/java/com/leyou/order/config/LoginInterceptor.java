package com.leyou.order.config;
import com.leyou.common.utils.CookieUtils;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {
    private JwtProperties prop;
    private final static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    private final static ThreadLocal<UserInfo> threadLocal=new ThreadLocal<>();
    public LoginInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //查询token
        String s = CookieUtils.getCookieValue(request, prop.getCookieName());
        if (StringUtils.isBlank(s)) {
            logger.error("用户没有登录");
            // 未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(s, prop.getPublicKey());
            threadLocal.set(userInfo);
            return true;
        } catch (Exception e) {
            logger.error("用户没有登录");
            // 未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
    public static UserInfo getUserInfo(){

        return threadLocal.get();
    }
}
