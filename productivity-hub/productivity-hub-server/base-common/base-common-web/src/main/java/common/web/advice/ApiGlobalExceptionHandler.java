package common.web.advice;

import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import org.apache.catalina.connector.ClientAbortException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

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
     * 处理客户端主动断开连接的异常。
     * 客户端在服务器响应完成前关闭连接是正常行为，不应该记录为错误。
     *
     * @param req HTTP请求对象
     * @param e   客户端断开连接异常对象
     * @return 响应对象（虽然客户端已断开，但避免 Spring 报错）
     */
    @ExceptionHandler(value = ClientAbortException.class)
    @ResponseBody
    public ApiResponse<Object> clientAbortExceptionHandler(HttpServletRequest req, ClientAbortException e) {
        // 使用 DEBUG 级别，因为这是正常的客户端行为（客户端主动断开连接）
        log.debug("客户端主动断开连接：{} - {}", req.getRequestURI(), e.getMessage());
        // 返回一个响应对象，避免 Spring 报错（虽然客户端已断开，收不到响应）
        return ApiResponse.ok(null);
    }

    /**
     * 处理 IO 异常（可能是客户端断开连接，也可能是其他 IO 错误）。
     *
     * @param req HTTP请求对象
     * @param e   IO异常对象
     * @return 响应对象
     */
    @ExceptionHandler(value = IOException.class)
    @ResponseBody
    public ApiResponse<Object> ioExceptionHandler(HttpServletRequest req, IOException e) {
        // 检查是否是客户端主动断开连接
        String message = e.getMessage();
        if (message != null && 
            (message.contains("你的主机中的软件中止了一个已建立的连接") ||
             message.contains("Broken pipe") ||
             message.contains("Connection reset by peer"))) {
            // 使用 DEBUG 级别，因为这是正常的客户端行为
            log.debug("客户端主动断开连接：{} - {}", req.getRequestURI(), message);
            return ApiResponse.ok(null);
        }
        // 其他 IOException 按普通异常处理
        log.error("IO异常！原因是:", e);
        return ApiResponse.fail("服务器内部错误");
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

    /**
     * 处理参数校验异常（@Valid注解校验失败）.
     * 关联需求：全部
     * 关联接口：全部
     *
     * @param req HTTP请求对象
     * @param e   参数校验异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ApiResponse<Object> methodArgumentNotValidExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", errorMessage);
        return ApiResponse.fail(400, errorMessage);
    }

    /**
     * 处理参数绑定异常（@Valid注解校验失败）.
     * 关联需求：全部
     * 关联接口：全部
     *
     * @param req HTTP请求对象
     * @param e   参数绑定异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ApiResponse<Object> bindExceptionHandler(HttpServletRequest req, BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", errorMessage);
        return ApiResponse.fail(400, errorMessage);
    }
}

