package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.vo.ExceptionBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;


@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionBody> exception(LyException e){
        ExceptionEnum exceptionEnum = e.getExceptionEnum();
        return ResponseEntity.status(exceptionEnum.getCode()).body(new ExceptionBody(exceptionEnum));
    }
}
