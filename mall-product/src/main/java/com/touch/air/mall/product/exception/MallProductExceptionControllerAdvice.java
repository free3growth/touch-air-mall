package com.touch.air.mall.product.exception;

import com.touch.air.common.exception.BizCodeEnum;
import com.touch.air.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 *
 * @author: bin.wang
 * @date: 2020/12/17 16:31
 *
 * @RestControllerAdvice 等价于 @ControllerAdvice和@ResponseBody
 * 处理全局异常并以json格式返回
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.touch.air.mall.product.controller")
public class MallProductExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题，异常类型：{}", e.getMessage(), e.getClass());
        Map<String, String> errorMap = new HashMap<>();
            //1.获取校验的错误结果
            e.getBindingResult().getFieldErrors().forEach((item)-> {
                //2.获取错误提示
                String message = item.getDefaultMessage();
                //3.获取错误的属性名字
                String field = item.getField();
                errorMap.put(field, message);
            });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        return R.error(throwable.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error("未知异常："+e.getMessage());
    }

}
