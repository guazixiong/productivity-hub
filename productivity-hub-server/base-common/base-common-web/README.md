# base-common-web 模块

Web 通用组件模块，提供 Spring MVC 相关的通用功能。

## 功能

- **JWT 认证拦截器** (`JwtAuthInterceptor`): 自动验证请求中的 JWT Token
- **全局异常处理器** (`ApiGlobalExceptionHandler`): 统一处理异常并返回 `ApiResponse` 格式
- **Web MVC 配置** (`WebMvcConfig`): 自动配置拦截器和 CORS

## 使用方式

### 1. 添加依赖

在业务模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.pbad</groupId>
    <artifactId>base-common-web</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置（可选）

在 `application.yml` 中配置：

```yaml
common:
  web:
    enabled: true                    # 是否启用（默认：true）
    api-path-prefix: /api            # API 路径前缀（默认：/api）
    exclude-paths: /api/auth/login   # 排除的路径，逗号分隔（默认：/api/auth/login）
```

### 3. 自定义异常处理（可选）

如果需要业务特定的异常处理，可以创建子类：

```java
@ControllerAdvice(basePackages = {"com.pbad.auth.controller", "com.pbad.config.controller"})
public class BusinessExceptionHandler extends ApiGlobalExceptionHandler {
    // 可以添加业务特定的异常处理逻辑
}
```

## 组件说明

### JwtAuthInterceptor

JWT 认证拦截器，自动验证请求头中的 `Authorization: Bearer {token}`。

**特性**：
- 支持配置排除路径
- 自动提取用户信息到请求属性（`userId`, `username`）

### ApiGlobalExceptionHandler

全局异常处理器，统一处理以下异常：
- `BusinessException`: 业务异常
- `NullPointerException`: 空指针异常
- `NumberFormatException`: 数字格式异常
- `Exception`: 其他异常

所有异常都会返回 `ApiResponse` 格式的响应。

### WebMvcConfig

Web MVC 配置类，自动配置：
- JWT 认证拦截器
- CORS 跨域支持

通过 `@ConditionalOnProperty` 控制是否启用，默认启用。

