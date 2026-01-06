package com.pbad.image.domain.dto;

import lombok.Data;

/**
 * 图片分享DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ImageShareDTO {

    /**
     * 过期时间（yyyy-MM-dd HH:mm:ss），为空表示永久有效
     */
    private String expiresAt;
}

