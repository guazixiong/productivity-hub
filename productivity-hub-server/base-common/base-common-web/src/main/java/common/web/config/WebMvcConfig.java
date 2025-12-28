package common.web.config;

import common.web.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC 配置.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "common.web.enabled", havingValue = "true", matchIfMissing = true)
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * API 路径前缀（默认：/api，可通过配置 common.web.api-path-prefix 修改）
     */
    @Value("${common.web.api-path-prefix:/api}")
    private String apiPathPrefix;

    /**
     * 排除的路径（默认：/api/auth/login，可通过配置 common.web.exclude-paths 修改，逗号分隔）
     */
    @Value("${common.web.exclude-paths:/api/auth/login}")
    private String excludePathsConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 解析排除路径配置
        List<String> excludePaths = Arrays.stream(excludePathsConfig.split(","))
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        jwtAuthInterceptor.setExcludePaths(excludePaths);

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(apiPathPrefix + "/**")
                .excludePathPatterns(excludePaths.toArray(new String[0]));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(apiPathPrefix + "/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

