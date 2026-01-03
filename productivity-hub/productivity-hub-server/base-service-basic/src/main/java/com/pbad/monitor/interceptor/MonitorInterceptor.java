package com.pbad.monitor.interceptor;

import com.pbad.monitor.service.impl.MonitorServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 监控拦截器
 * 用于记录请求指标
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Slf4j
@Component
public class MonitorInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    private MonitorServiceImpl monitorService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (monitorService != null) {
            request.setAttribute("startTime", System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (monitorService != null) {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                String path = request.getRequestURI();
                boolean success = response.getStatus() < 400;
                monitorService.recordRequest(path, success, responseTime);
            }
        }
    }
}

