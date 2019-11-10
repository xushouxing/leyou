package com.leyou.common.vo;

import com.leyou.common.myenum.ExceptionEnum;

public class   ExceptionBody {
    int code;
    String message;
    Long timetamp;
     public ExceptionBody(ExceptionEnum em){
        this.code=em.getCode();
        this.message=em.getMessage();
        this.timetamp=System.currentTimeMillis();
    }
}
