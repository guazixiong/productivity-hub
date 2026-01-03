package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.AssetSettingsUpdateDTO;
import com.pbad.asset.domain.vo.AssetSettingsVO;
import com.pbad.asset.service.AssetSettingsService;
import com.pbad.config.domain.po.UserConfigPO;
import com.pbad.config.mapper.UserConfigMapper;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 资产设置服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSettingsServiceImpl implements AssetSettingsService {

    private static final String MODULE = "asset";
    private static final String EXCLUDE_RETIRED_KEY = "asset.excludeRetired";
    private static final String CALCULATE_PROFIT_KEY = "asset.calculateProfit";
    private static final Boolean DEFAULT_EXCLUDE_RETIRED = true;
    private static final Boolean DEFAULT_CALCULATE_PROFIT = true;

    private final UserConfigMapper userConfigMapper;

    @Override
    public AssetSettingsVO getAssetSettings() {
        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            // 返回默认值
            AssetSettingsVO vo = new AssetSettingsVO();
            vo.setExcludeRetired(DEFAULT_EXCLUDE_RETIRED);
            vo.setCalculateProfit(DEFAULT_CALCULATE_PROFIT);
            return vo;
        }

        AssetSettingsVO vo = new AssetSettingsVO();

        // 获取excludeRetired配置
        UserConfigPO excludeRetiredConfig = userConfigMapper.selectByUserAndKey(
                userId, MODULE, EXCLUDE_RETIRED_KEY);
        if (excludeRetiredConfig != null && excludeRetiredConfig.getConfigValue() != null) {
            vo.setExcludeRetired(Boolean.parseBoolean(excludeRetiredConfig.getConfigValue()));
        } else {
            vo.setExcludeRetired(DEFAULT_EXCLUDE_RETIRED);
        }

        // 获取calculateProfit配置
        UserConfigPO calculateProfitConfig = userConfigMapper.selectByUserAndKey(
                userId, MODULE, CALCULATE_PROFIT_KEY);
        if (calculateProfitConfig != null && calculateProfitConfig.getConfigValue() != null) {
            vo.setCalculateProfit(Boolean.parseBoolean(calculateProfitConfig.getConfigValue()));
        } else {
            vo.setCalculateProfit(DEFAULT_CALCULATE_PROFIT);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetSettingsVO updateAssetSettings(AssetSettingsUpdateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // 更新excludeRetired配置
        UserConfigPO excludeRetiredConfig = userConfigMapper.selectByUserAndKey(
                userId, MODULE, EXCLUDE_RETIRED_KEY);
        if (excludeRetiredConfig != null) {
            excludeRetiredConfig.setConfigValue(String.valueOf(dto.getExcludeRetired()));
            excludeRetiredConfig.setUpdatedAt(nowStr);
            excludeRetiredConfig.setUpdatedBy(userId);
            userConfigMapper.updateConfig(excludeRetiredConfig);
        } else {
            excludeRetiredConfig = new UserConfigPO();
            excludeRetiredConfig.setId(String.valueOf(System.currentTimeMillis()));
            excludeRetiredConfig.setUserId(userId);
            excludeRetiredConfig.setModule(MODULE);
            excludeRetiredConfig.setConfigKey(EXCLUDE_RETIRED_KEY);
            excludeRetiredConfig.setConfigValue(String.valueOf(dto.getExcludeRetired()));
            excludeRetiredConfig.setDescription("退役资产不计入总额");
            excludeRetiredConfig.setCreatedAt(nowStr);
            excludeRetiredConfig.setUpdatedAt(nowStr);
            excludeRetiredConfig.setUpdatedBy(userId);
            userConfigMapper.batchInsert(Arrays.asList(excludeRetiredConfig));
        }

        // 更新calculateProfit配置
        UserConfigPO calculateProfitConfig = userConfigMapper.selectByUserAndKey(
                userId, MODULE, CALCULATE_PROFIT_KEY);
        if (calculateProfitConfig != null) {
            calculateProfitConfig.setConfigValue(String.valueOf(dto.getCalculateProfit()));
            calculateProfitConfig.setUpdatedAt(nowStr);
            calculateProfitConfig.setUpdatedBy(userId);
            userConfigMapper.updateConfig(calculateProfitConfig);
        } else {
            calculateProfitConfig = new UserConfigPO();
            calculateProfitConfig.setId(String.valueOf(System.currentTimeMillis() + 1));
            calculateProfitConfig.setUserId(userId);
            calculateProfitConfig.setModule(MODULE);
            calculateProfitConfig.setConfigKey(CALCULATE_PROFIT_KEY);
            calculateProfitConfig.setConfigValue(String.valueOf(dto.getCalculateProfit()));
            calculateProfitConfig.setDescription("总资产算二手盈利");
            calculateProfitConfig.setCreatedAt(nowStr);
            calculateProfitConfig.setUpdatedAt(nowStr);
            calculateProfitConfig.setUpdatedBy(userId);
            userConfigMapper.batchInsert(Arrays.asList(calculateProfitConfig));
        }

        log.info("用户 {} 更新资产设置: excludeRetired={}, calculateProfit={}", 
                userId, dto.getExcludeRetired(), dto.getCalculateProfit());

        // 返回更新后的设置
        AssetSettingsVO vo = new AssetSettingsVO();
        vo.setExcludeRetired(dto.getExcludeRetired());
        vo.setCalculateProfit(dto.getCalculateProfit());
        return vo;
    }
}

