package common.web.interceptor;

import common.core.domain.ApiResponse;
import common.util.JwtUtil;
import common.web.context.RequestUser;
import common.web.context.RequestUserContext;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证拦截器.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    /**
     * 排除的路径（默认排除登录接口）
     */
    private List<String> excludePaths = Arrays.asList("/api/auth/login");

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

        // 获取 Token
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);

        if (token == null || !JwtUtil.validateToken(token)) {
            // Token 无效，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            ApiResponse<Object> apiResponse = ApiResponse.unauthorized("Token 无效或过期");
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(apiResponse));
            writer.flush();
            writer.close();

            return false;
        }

        // 将用户信息存储到请求属性中，供后续使用
        String userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        RequestUserContext.set(RequestUser.builder().userId(userId).username(username).build());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestUserContext.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 支持前缀匹配（配置以 /** 结尾）和精确匹配.
     */
    private boolean matchesExcludePath(String requestURI, String excludePath) {
        if (excludePath.endsWith("/**")) {
            String prefix = excludePath.substring(0, excludePath.length() - 3);
            return requestURI.startsWith(prefix);
        }
        return requestURI.startsWith(excludePath);
    }
}

