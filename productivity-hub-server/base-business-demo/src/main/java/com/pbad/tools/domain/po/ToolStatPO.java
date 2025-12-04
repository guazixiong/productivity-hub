package com.pbad.tools.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 工具统计实体类（PO）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ToolStatPO {
    /**
     * 工具ID
     */
    private String id;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 点击次数
     */
    private Integer clicks;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

