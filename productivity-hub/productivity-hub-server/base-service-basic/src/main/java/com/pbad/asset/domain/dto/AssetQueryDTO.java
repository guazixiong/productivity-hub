package com.pbad.asset.domain.dto;

import lombok.Data;

/**
 * 资产查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetQueryDTO {
    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量（默认20）
     */
    private Integer pageSize = 20;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 状态（IN_SERVICE、RETIRED、SOLD）
     */
    private String status;
}

