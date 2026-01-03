package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 资产批量删除DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetBatchDeleteDTO {
    /**
     * 操作类型，固定值"delete"
     */
    @NotBlank(message = "操作类型不能为空")
    private String action;

    /**
     * 资产ID列表
     */
    @NotEmpty(message = "资产ID列表不能为空")
    private List<String> ids;
}

