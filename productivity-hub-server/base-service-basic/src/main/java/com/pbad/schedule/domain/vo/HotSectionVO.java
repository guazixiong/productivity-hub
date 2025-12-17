package com.pbad.schedule.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 热点板块 VO.
 */
@Data
public class HotSectionVO {
    private String name;
    private List<HotItemVO> items;
}


