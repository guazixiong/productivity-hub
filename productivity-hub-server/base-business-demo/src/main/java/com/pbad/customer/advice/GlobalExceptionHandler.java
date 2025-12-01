package com.pbad.customer.advice;

import common.core.domain.Response;
import common.exception.BusinessException;
import common.exception.ExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器，用于处理不同类型的异常情况。
 *
 * @author: pangdi
 * @date: 2023/9/8 15:04
 * @version: 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常。
     *
     * @param req HTTP请求对象
     * @param e   业务异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public Response<Void> bizExceptionHandler(HttpServletRequest req, BusinessException e) {
        logger.error("发生业务异常！原因是：{}", e.getMessage());
        return Response.fail(Integer.parseInt(e.getErrorCode()), e.getMessage());
    }

    /**
     * 处理空指针的异常。
     *
     * @param req HTTP请求对象
     * @param e   空指针异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public Response<Void> exceptionHandler(HttpServletRequest req, NullPointerException e) {
        logger.error("发生空指针异常！原因是:", e);
        return Response.fail(ExceptionEnum.BODY_NOT_MATCH);
    }

    /**
     * 处理其他异常。
     *
     * @param req HTTP请求对象
     * @param e   其他异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response<Void> exceptionHandler(HttpServletRequest req, Exception e) {
        logger.error("未知异常！原因是:", e);
        return Response.fail(ExceptionEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理类型转换异常。
     *
     * @param req HTTP请求对象
     * @param e   类型转换异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = NumberFormatException.class)
    @ResponseBody
    public Response<Void> exceptionHandler(HttpServletRequest req, NumberFormatException e) {
        logger.error("发生类型转换异常！原因是:", e);
        return Response.fail(ExceptionEnum.PARAMS_NOT_CONVERT);
    }
}
