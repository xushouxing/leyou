package com.leyou.common.myenum;

public enum  ExceptionEnum {
    PRICE_NOT_BE_NULL("价格不能为空",400),
    CATEGORY_NOT_BE_FOUND("商品分类没查到",400),
    BRAND_NOT_BE_FOUND("品牌没有查到",400),
    BRAND_INSERT_FAIL("商品新增失败",400),
    FILE_UPLOAD_FAIL("文件上传失败",400),
    SPEC_GROUP_NOT_FUOND("商品组信息没有查带",400),
    SPEC_PARAMS_NOT_FOUND("商品参数没有查到",400),
    SPU_NOT_BE_FOUND("spu没有查到",400),
    SPUDETAIL_NOT_BT_FOUND("spuDetail没有查到",400),
    CATEGORIES_FOUNDBYCIDS_FALIS("根据cids查询分类失败",400),
    CODE_SEND_FAIL("验证码发送失败",400),
    CODE_NOT_SAVE("验证码不一致",400),
    USERNAME_PASSWORD_NOT_SAVE("用户名或密码错误",400),
    TOKEN_FAIL("thoken认证失败",400),
    PUBLICKE_FAIL("公钥初始化失败",400),
    USERNOTIN("用户没有权限",400),
    ORDER_FAIL("订单写入失败",400),
    ORDERDTEAIL_FAIL("订单详情写入失败",400),
    ORDERSTATUS_FAIL("订单状态查询失败",400),
    WXPAY_FAIL("微信支付下单通信异常",500),
    SING_FAIL("签名有误",500),
    ORDER_PARAM_INVALID("订单参数异常",500),
    ORDER_MONEY_INVALID("订单金额异常",500),
    ORDER_STATUS_UPDATE_FAIL("订单状态修改异常",500),
    ;
    String message;
    Integer code;

    ExceptionEnum() {
    }

    ExceptionEnum(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
