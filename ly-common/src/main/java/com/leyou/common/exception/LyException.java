package com.leyou.common.exception;

import com.leyou.common.myenum.ExceptionEnum;

public class LyException extends RuntimeException {
    ExceptionEnum exceptionEnum;
   public LyException(ExceptionEnum exceptionEnum){
        this.exceptionEnum=exceptionEnum;
    }

    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public void setExceptionEnum(ExceptionEnum exceptionEnum) {
        this.exceptionEnum = exceptionEnum;
    }
}
