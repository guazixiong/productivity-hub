package common.web.interceptor;

import common.core.domain.ApiResponse;
import common.web.config.CircuitBreakerProperties;
import common.web.config.RateLimitProperties;
import common.web.context.RequestUser;
import common.web.context.RequestUserContext;
import common.web.util.CircuitBreaker;
import common.web.util.RateLimitUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 限流与降级拦截器.
 * <p>支持按用户ID、接口路径、IP地址三个维度进行限流，并集成熔断器.</p>
 *
 * @author pbad
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Autowired
    private CircuitBreakerProperties circuitBreakerProperties;

    /**
     * 排除的路径（默认排除登录接口）
     */
    private List<String> excludePaths = Arrays.asList("/api/auth/login", "/api/auth/captcha");

    /**
     * 熔断器缓存（key: apiPath）
     */
    private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * 设置排除的路径
     *
     * @param excludePaths 排除的路径列表
     */
    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否在排除路径中
        String requestURI = request.getRequestURI();
        for (String excludePath : excludePaths) {
            if (matchesExcludePath(requestURI, excludePath)) {
                return true;
            }
        }

        // 如果限流未启用，直接通过
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        // 获取用户ID（从请求属性或上下文）
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            RequestUser requestUser = RequestUserContext.get();
            if (requestUser != null) {
                userId = requestUser.getUserId();
            }
        }

        // 获取IP地址
        String ip = getClientIp(request);

        // 1. 检查熔断器状态
        if (circuitBreakerProperties.isEnabled()) {
            CircuitBreaker circuitBreaker = getCircuitBreaker(requestURI);
            if (!circuitBreaker.allowRequest()) {
                log.warn("[RateLimitInterceptor] 熔断器已开启，拒绝请求：{}，用户ID：{}，IP：{}", requestURI, userId, ip);
                sendRateLimitResponse(response, "服务暂时不可用，请稍后重试");
                return false;
            }
            // 在半开状态下增加请求计数
            if (circuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN) {
                circuitBreaker.incrementHalfOpenRequest();
            }
        }

        // 2. 检查用户级别限流
        if (userId != null && !userId.isEmpty()) {
            if (!RateLimitUtil.tryAcquireForUser(userId, rateLimitProperties.getUser().getQps())) {
                log.warn("[RateLimitInterceptor] 用户级别限流触发：用户ID={}，QPS限制={}", userId, rateLimitProperties.getUser().getQps());
                sendRateLimitResponse(response, "请求过于频繁，请稍后重试");
                return false;
            }
        }

        // 3. 检查接口级别限流
        if (!RateLimitUtil.tryAcquireForApi(requestURI, rateLimitProperties.getApi().getQps())) {
            log.warn("[RateLimitInterceptor] 接口级别限流触发：接口={}，QPS限制={}", requestURI, rateLimitProperties.getApi().getQps());
            sendRateLimitResponse(response, "请求过于频繁，请稍后重试");
            return false;
        }

        // 4. 检查IP级别限流
        if (!RateLimitUtil.tryAcquireForIp(ip, rateLimitProperties.getIp().getQps())) {
            log.warn("[RateLimitInterceptor] IP级别限流触发：IP={}，QPS限制={}", ip, rateLimitProperties.getIp().getQps());
            sendRateLimitResponse(response, "请求过于频繁，请稍后重试");
            return false;
        }

        // 记录请求开始时间（用于计算响应时间）
        request.setAttribute("rateLimitStartTime", System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 如果熔断器启用，记录请求结果
        if (circuitBreakerProperties.isEnabled()) {
            String requestURI = request.getRequestURI();
            CircuitBreaker circuitBreaker = getCircuitBreaker(requestURI);

            // 检查是否超时
            Long startTime = (Long) request.getAttribute("rateLimitStartTime");
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                if (responseTime > circuitBreakerProperties.getTimeout()) {
                    circuitBreaker.recordTimeout();
                    log.warn("[RateLimitInterceptor] 请求超时：{}，响应时间：{}ms", requestURI, responseTime);
                    return;
                }
            }

            // 记录请求结果
            if (ex != null || response.getStatus() >= 500) {
                circuitBreaker.recordFailure();
            } else {
                circuitBreaker.recordSuccess();
            }
        }
    }

    /**
     * 获取或创建熔断器
     */
    private CircuitBreaker getCircuitBreaker(String apiPath) {
        return circuitBreakers.computeIfAbsent(apiPath, k -> new CircuitBreaker(
                apiPath,
                circuitBreakerProperties.getErrorThreshold(),
                circuitBreakerProperties.getTimeout(),
                circuitBreakerProperties.getHalfOpenRequests(),
                circuitBreakerProperties.getOpenDuration()
        ));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * 发送限流响应（429状态码）
     */
    private void sendRateLimitResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Object> apiResponse = ApiResponse.fail(429, message);
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(apiResponse));
        writer.flush();
        writer.close();
    }

    /**
     * 支持前缀匹配（配置以 /** 结尾）和精确匹配
     */
    private boolean matchesExcludePath(String requestURI, String excludePath) {
        if (excludePath.endsWith("/**")) {
            String prefix = excludePath.substring(0, excludePath.length() - 3);
            return requestURI.startsWith(prefix);
        }
        return requestURI.equals(excludePath);
    }
}

