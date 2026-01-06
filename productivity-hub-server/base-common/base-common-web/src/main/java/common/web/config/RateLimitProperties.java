package common.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置属性.
 *
 * @author pbad
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * 是否启用限流（默认：true）
     */
    private boolean enabled = true;

    /**
     * 用户级别限流配置
     */
    private LimitConfig user = new LimitConfig(100);

    /**
     * 接口级别限流配置
     */
    private LimitConfig api = new LimitConfig(1000);

    /**
     * IP级别限流配置
     */
    private LimitConfig ip = new LimitConfig(50);

    /**
     * 限流配置内部类
     */
    @Data
    public static class LimitConfig {
        /**
         * QPS（每秒请求数）
         */
        private double qps;

        public LimitConfig() {
        }

        public LimitConfig(double qps) {
            this.qps = qps;
        }
    }
}

