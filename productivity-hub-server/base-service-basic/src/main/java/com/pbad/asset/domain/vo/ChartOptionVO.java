package com.pbad.asset.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * ECharts图表配置视图对象（VO）.
 *
 * <p>用于直接返回ECharts图表配置，前端无需处理数据，直接渲染即可。</p>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ChartOptionVO {
    /**
     * ECharts配置对象（JSON格式）
     * 使用Map结构，Spring会自动序列化为JSON
     */
    private Map<String, Object> option;
}

