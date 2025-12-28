package com.pbad.tools.domain.vo;

import lombok.Data;

/**
 * 工具统计 VO.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ToolStatVO {
    /**
     * 工具ID
     */
    private String id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 点击次数
     */
    private Integer clicks;
}

