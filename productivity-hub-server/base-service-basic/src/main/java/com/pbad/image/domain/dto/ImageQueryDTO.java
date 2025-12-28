package com.pbad.image.domain.dto;

import lombok.Data;

/**
 * 图片查询DTO.
 * 关联需求：REQ-IMG-002
 * 关联接口：API-REQ-IMG-002-01
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ImageQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认20，最大100
     */
    private Integer pageSize = 20;

    /**
     * 图片分类筛选
     */
    private String category;

    /**
     * 业务模块筛选
     */
    private String businessModule;

    /**
     * 业务关联ID筛选
     */
    private String businessId;

    /**
     * 关键词搜索（文件名、描述）
     */
    private String keyword;

    /**
     * 开始时间（yyyy-MM-dd）
     */
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd）
     */
    private String endTime;

    /**
     * 状态筛选（ACTIVE、DELETED、ARCHIVED），默认ACTIVE
     */
    private String status = "ACTIVE";

    /**
     * 排序字段（createTime、fileSize、accessCount），默认createTime
     */
    private String sortBy = "createTime";

    /**
     * 排序方向（asc、desc），默认desc
     */
    private String sortOrder = "desc";
}

