package com.pbad.image.domain.dto;

import lombok.Data;

/**
 * 图片更新DTO.
 * 关联需求：REQ-IMG-003
 * 关联接口：API-REQ-IMG-003-01
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ImageUpdateDTO {

    /**
     * 图片描述
     */
    private String description;

    /**
     * 业务模块标识
     */
    private String businessModule;

    /**
     * 业务关联ID
     */
    private String businessId;
}

