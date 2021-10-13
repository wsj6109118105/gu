package com.product.exception;

import com.common.exception.BizCodeException;
import com.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * user:lufei
 * DATE:2021/10/13
 **/

/**
 * 集中处理所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.product.controller"})
public class guExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型{}",e.getMessage(),e.getClass());
        Map<String,String> map = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach(item->{
            String message = item.getDefaultMessage();
            String field = item.getField();
            map.put(field,message);
        });
        return R.error(BizCodeException.VALID_EXCEPTION.getCode(), BizCodeException.VALID_EXCEPTION.getMsg()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e){
        log.error("数据校验出现问题{}，异常类型{}",e.getMessage(),e.getClass());
        return R.error(BizCodeException.UNKNOW_EXCEPTION.getCode(), BizCodeException.UNKNOW_EXCEPTION.getMsg());
    }
}
