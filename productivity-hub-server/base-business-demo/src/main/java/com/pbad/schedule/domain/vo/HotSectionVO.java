package com.pbad.schedule.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 热点板块 VO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HotSectionVO {
    /**
     * 板块名称（如：知乎热搜、微博热搜等）
     */
    private String name;

    /**
     * 热点项列表
     */
    private List<HotItemVO> items;
}

