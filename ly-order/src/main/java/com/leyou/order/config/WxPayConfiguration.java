package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxPayConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "leyou.pay")
    public PayConfig payConfig(){
        return new PayConfig();
    }

    /**
     * 参数1 微信支付配置类
     * 参数2 签名类型
     * @return
     */
    @Bean
    public WXPay wxPay(){
        return new WXPay(payConfig(),WXPayConstants.SignType.HMACSHA256);
    }
}
