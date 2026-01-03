package com.pbad.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker/DataCenter 配置属性.
 */
@Data
@ConfigurationProperties(prefix = "id-generator")
public class IdGeneratorProperties {

    /**
     * 默认工作ID.
     */
    private long defaultWorkerId = 0;

    /**
     * 默认数据中心ID.
     */
    private long defaultDatacenterId = 0;

    /**
     * 各模块专属配置.
     */
    private Map<String, ModuleConfig> modules = new HashMap<>();

    @Data
    public static class ModuleConfig {
        /**
         * 模块工作ID.
         */
        private long workerId = 0;

        /**
         * 模块数据中心ID.
         */
        private long datacenterId = 0;
    }
}

