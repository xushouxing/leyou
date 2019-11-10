package com.leyou.order.config;

import com.github.wxpay.sdk.WXPayConfig;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.InputStream;
@AllArgsConstructor
@NoArgsConstructor
public class PayConfig implements WXPayConfig {
    private String appId;  //公众账号
    private String mchId;   //账户号
    private String key;     //密钥
    private int httpConnectTimeoutMs; //链接超时时间
    private int httpReadTimeoutMs;    //读取超时时间
    @Override
    public String getAppID() {
        return appId;
    }

    @Override
    public String getMchID() {
        return mchId;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return httpConnectTimeoutMs;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return httpReadTimeoutMs;
    }
}
