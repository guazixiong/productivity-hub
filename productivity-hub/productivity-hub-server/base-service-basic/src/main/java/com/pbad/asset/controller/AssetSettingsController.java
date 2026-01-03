package com.pbad.asset.controller;

import com.pbad.asset.domain.dto.AssetSettingsUpdateDTO;
import com.pbad.asset.domain.vo.AssetSettingsVO;
import com.pbad.asset.service.AssetSettingsService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资产设置控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/settings/asset")
@RequiredArgsConstructor
public class AssetSettingsController {

    private final AssetSettingsService assetSettingsService;

    /**
     * 获取资产设置
     */
    @GetMapping
    public ApiResponse<AssetSettingsVO> getAssetSettings() {
        AssetSettingsVO settings = assetSettingsService.getAssetSettings();
        return ApiResponse.ok(settings);
    }

    /**
     * 更新资产设置
     */
    @PostMapping
    public ApiResponse<AssetSettingsVO> updateAssetSettings(@RequestBody AssetSettingsUpdateDTO dto) {
        AssetSettingsVO settings = assetSettingsService.updateAssetSettings(dto);
        return ApiResponse.ok(settings);
    }
}

