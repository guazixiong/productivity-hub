package com.pbad.config;

import org.springframework.context.annotation.Configuration;

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
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Configuration
public class BusinessWebConfig {
    // 当前使用 base-common-web 的默认配置
    // 如需自定义，请参考上面的注释
}

