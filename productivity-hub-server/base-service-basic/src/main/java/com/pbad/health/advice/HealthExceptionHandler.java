package com.pbad.health.advice;

import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 健康模块异常处理器.
 * 关联需求：REQ-HEALTH-001, REQ-HEALTH-002, REQ-HEALTH-003, REQ-HEALTH-004, REQ-HEALTH-005
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@ControllerAdvice(basePackages = "com.pbad.health.controller")
public class HealthExceptionHandler {

    /**
     * 健康模块错误码范围：40001-40016
     */
    private static final int HEALTH_ERROR_CODE_BASE = 40001;
    private static final int HEALTH_ERROR_CODE_MAX = 40016;

    /**
     * 处理健康模块的业务异常.
     * 将健康模块的异常映射到特定的错误码范围（40001-40016）
     *
     * @param req HTTP请求对象
     * @param e   业务异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ApiResponse<Object> healthBizExceptionHandler(HttpServletRequest req, BusinessException e) {
        String requestPath = req.getRequestURI();
        String errorCode = e.getErrorCode();
        String errorMessage = e.getMessage();

        // 记录详细的异常日志（不暴露敏感信息）
        log.error("健康模块业务异常 - 请求路径: {}, 错误码: {}, 错误信息: {}", 
                requestPath, errorCode, errorMessage);

        // 映射错误码到健康模块错误码范围
        int code = mapToHealthErrorCode(errorCode);

        // 确保错误信息不暴露敏感信息（如SQL、文件路径等）
        String safeMessage = sanitizeErrorMessage(errorMessage);

        return ApiResponse.fail(code, safeMessage);
    }

    /**
     * 处理健康模块的其他异常.
     *
     * @param req HTTP请求对象
     * @param e   异常对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResponse<Object> healthExceptionHandler(HttpServletRequest req, Exception e) {
        String requestPath = req.getRequestURI();

        // 记录详细的异常日志
        log.error("健康模块未知异常 - 请求路径: {}", requestPath, e);

        // 不暴露敏感信息，返回通用错误信息
        return ApiResponse.fail(HEALTH_ERROR_CODE_BASE, "健康模块服务异常，请稍后重试");
    }

    /**
     * 将错误码映射到健康模块错误码范围（40001-40016）
     *
     * @param errorCode 原始错误码
     * @return 映射后的错误码
     */
    private int mapToHealthErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return HEALTH_ERROR_CODE_BASE;
        }

        try {
            int code = Integer.parseInt(errorCode);

            // 如果错误码在健康模块范围内，直接返回
            if (code >= HEALTH_ERROR_CODE_BASE && code <= HEALTH_ERROR_CODE_MAX) {
                return code;
            }

            // 根据HTTP状态码映射到健康模块错误码
            // 400 -> 40001, 401 -> 40002, 403 -> 40003, 404 -> 40004, 500 -> 40005
            switch (code) {
                case 400:
                    return 40001; // 参数错误
                case 401:
                    return 40002; // 未授权
                case 403:
                    return 40003; // 无权限
                case 404:
                    return 40004; // 资源不存在
                case 500:
                    return 40005; // 服务器错误
                default:
                    // 其他错误码映射到基础错误码
                    return HEALTH_ERROR_CODE_BASE;
            }
        } catch (NumberFormatException e) {
            log.warn("无法解析错误码: {}", errorCode);
            return HEALTH_ERROR_CODE_BASE;
        }
    }

    /**
     * 清理错误信息，移除敏感信息
     *
     * @param errorMessage 原始错误信息
     * @return 清理后的错误信息
     */
    private String sanitizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "操作失败";
        }

        // 移除可能包含敏感信息的SQL错误
        if (errorMessage.contains("SQL") || errorMessage.contains("sql")) {
            return "数据操作失败，请稍后重试";
        }

        // 移除文件路径信息
        if (errorMessage.contains("File") || errorMessage.contains("file") || 
            errorMessage.contains("Path") || errorMessage.contains("path")) {
            return "文件操作失败，请稍后重试";
        }

        // 移除堆栈跟踪信息
        if (errorMessage.contains("Exception") || errorMessage.contains("at ")) {
            // 只保留第一行错误信息
            int newlineIndex = errorMessage.indexOf('\n');
            if (newlineIndex > 0) {
                return errorMessage.substring(0, newlineIndex).trim();
            }
        }

        return errorMessage;
    }
}

