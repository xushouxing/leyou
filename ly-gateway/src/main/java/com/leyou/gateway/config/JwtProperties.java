package com.leyou.gateway.config;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.utils.RsaUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "leyou.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;
    private final static Logger logger=LoggerFactory.getLogger(JwtProperties.class);
    @PostConstruct
    public void init(){
        try {
            this.publicKey=RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            logger.error("公钥初始化失败{}",pubKeyPath);
            throw new LyException(ExceptionEnum.PUBLICKE_FAIL);
        }
    }
}
