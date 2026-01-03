package common.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 熔断器配置属性.
 *
 * @author pbad
 */
@Data
@Component
@ConfigurationProperties(prefix = "circuit-breaker")
public class CircuitBreakerProperties {

    /**
     * 是否启用熔断（默认：true）
     */
    private boolean enabled = true;

    /**
     * 错误率阈值（默认：0.5，即50%）
     */
    private double errorThreshold = 0.5;

    /**
     * 超时时间（毫秒，默认：5000）
     */
    private long timeout = 5000;

    /**
     * 半开状态尝试恢复的请求数（默认：10）
     */
    private int halfOpenRequests = 10;

    /**
     * 熔断持续时间（毫秒，默认：60000，即1分钟）
     */
    private long openDuration = 60000;
}

