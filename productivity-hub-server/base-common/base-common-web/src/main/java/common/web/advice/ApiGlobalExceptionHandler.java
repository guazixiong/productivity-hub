package common.web.advice;

import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * API 全局异常处理器（使用 ApiResponse 格式）.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@ControllerAdvice
public class ApiGlobalExceptionHandler {

    /**
     * 处理自定义的业务异常。
     *
     * @param req HTTP请求对象
     * @param e   业务异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ApiResponse<Object> bizExceptionHandler(HttpServletRequest req, BusinessException e) {
        log.error("发生业务异常！原因是：{}", e.getMessage());
        String errorCode = e.getErrorCode();
        int code = errorCode != null && !errorCode.isEmpty() 
                ? Integer.parseInt(errorCode) 
                : ApiResponse.INTERNAL_ERROR;
        return ApiResponse.fail(code, e.getMessage());
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
    public ApiResponse<Object> exceptionHandler(HttpServletRequest req, NullPointerException e) {
        log.error("发生空指针异常！原因是:", e);
        return ApiResponse.fail("参数不能为空");
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
    public ApiResponse<Object> exceptionHandler(HttpServletRequest req, Exception e) {
        log.error("未知异常！原因是:", e);
        return ApiResponse.fail("服务器内部错误");
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
    public ApiResponse<Object> exceptionHandler(HttpServletRequest req, NumberFormatException e) {
        log.error("发生类型转换异常！原因是:", e);
        return ApiResponse.fail("参数格式错误");
    }
}

