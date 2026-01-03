package com.pbad.config;

import com.pbad.monitor.interceptor.MonitorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 业务特定的 Web 配置（扩展 base-common-web 的通用配置）.
 *
 * base-common-web 中的 WebMvcConfig 已经通过自动配置生效。
 * 如果需要自定义配置，可以在 application.yml 中设置：
 * <pre>
 * common.web.enabled=true
 * common.web.api-path-prefix=/api
 * common.web.exclude-paths=/api/auth/login
 * </pre>
 *
 * 如果需要业务特定的异常处理，可以创建子类并指定 basePackages：
 * <pre>
 * {@code
 * @ControllerAdvice(basePackages = {"com.pbad.auth.controller", "com.pbad.config.controller"})
 * public class BusinessExceptionHandler extends ApiGlobalExceptionHandler {
 *     // 可以添加业务特定的异常处理逻辑
 * }
 * }
 * </pre>
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Configuration
public class BusinessWebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private MonitorInterceptor monitorInterceptor;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Value("${app.upload.avatar-path-prefix:/uploads/avatars}")
    private String avatarPathPrefix;

    @Value("${app.image.base-dir:uploads/images}")
    private String imageBaseDir;

    @Value("${app.image.url-prefix:/uploads/images}")
    private String imageUrlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册监控拦截器（记录请求指标）
        if (monitorInterceptor != null) {
            registry.addInterceptor(monitorInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/monitor/**", "/actuator/**");
        }
    }

    /**
     * 配置静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 头像文件访问路径映射
        String avatarPath = new File(avatarUploadDir).getAbsolutePath();
        registry.addResourceHandler(avatarPathPrefix + "/**")
                .addResourceLocations("file:" + avatarPath + File.separator)
                .setCachePeriod(7 * 24 * 60 * 60); // 缓存7天

        // 图片文件访问路径映射（原图和缩略图）
        String imagePath = new File(imageBaseDir).getAbsolutePath();
        registry.addResourceHandler(imageUrlPrefix + "/**")
                .addResourceLocations("file:" + imagePath + File.separator)
                .setCachePeriod(7 * 24 * 60 * 60); // 原图缓存7天

        // 缩略图单独配置，缓存时间更长
        registry.addResourceHandler(imageUrlPrefix + "/**/thumbnails/**")
                .addResourceLocations("file:" + imagePath + File.separator)
                .setCachePeriod(30 * 24 * 60 * 60); // 缩略图缓存30天
    }
}

