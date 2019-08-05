package com.lzx.common.exception;


import com.lzx.common.result.CodeMsg;
import com.lzx.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器（和AOP一样）
 * 在异常发生时，会调用里面的方法给客户端响应
 */
@ControllerAdvice
@ResponseBody
public class GlobleExceptionHandler {


    private static final Logger log = LoggerFactory.getLogger(GlobleExceptionHandler.class);

    /**
     * 异常处理
     *
     * @param httpServletRequest
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class) //指定对何种异常处理
    public Result<String> exceptionHandler(HttpServletRequest httpServletRequest, Exception e) {
        log.info(e.toString());
        e.printStackTrace();
        if (e instanceof GlobleException) {
            GlobleException globleException = (GlobleException) e;
            return Result.error(globleException.getCm());
        } else if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            List<ObjectError> errors = bindException.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
