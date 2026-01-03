package com.pbad.asset.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 资产分页视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetPageVO {
    /**
     * 资产列表
     */
    private List<AssetListVO> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}

