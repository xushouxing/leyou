package com.leyou.common.myenum;

public enum OrderStatusEnum {
    UN_PAY(1,"未付款"),
    PAYED(2,"已付款，未发货"),
    UN_CONFIRN(3,"已付款，未确认"),
    CONFIRMED(4,"已确认，未评价"),
    CLOSED(5,"已关闭"),
    PATED(6,"已评价，交易结束")
    ;
    private Integer code;
    private String  msg;

    OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public Integer code(){
        return code;
    }
}
