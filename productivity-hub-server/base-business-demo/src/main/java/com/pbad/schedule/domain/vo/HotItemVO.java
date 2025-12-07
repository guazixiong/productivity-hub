package com.pbad.schedule.domain.vo;

import lombok.Data;

/**
 * 热点项 VO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HotItemVO {
    /**
     * 标题
     */
    private String title;

    /**
     * 链接
     */
    private String link;

    /**
     * 热度
     */
    private String heat;

    /**
     * 描述
     */
    private String desc;
}

