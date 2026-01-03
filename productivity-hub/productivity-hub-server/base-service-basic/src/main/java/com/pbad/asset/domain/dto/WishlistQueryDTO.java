package com.pbad.asset.domain.dto;

import lombok.Data;

/**
 * 心愿单查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WishlistQueryDTO {
    /**
     * 是否已实现
     */
    private Boolean achieved;
}

