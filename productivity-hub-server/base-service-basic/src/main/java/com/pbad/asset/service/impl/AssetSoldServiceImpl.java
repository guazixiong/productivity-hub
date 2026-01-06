package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.AssetSoldCreateDTO;
import com.pbad.asset.domain.dto.AssetSoldUpdateDTO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.po.AssetSoldPO;
import com.pbad.asset.domain.vo.AssetSoldVO;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.AssetSoldMapper;
import com.pbad.asset.service.AssetSoldService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 资产卖出服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class AssetSoldServiceImpl implements AssetSoldService {

    private final AssetSoldMapper soldMapper;
    private final AssetMapper assetMapper;
    private final IdGeneratorApi idGeneratorApi;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未登录");
        }
        return userId;
    }

    /**
     * 解析日期字符串
     */
    private Date parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
        }
    }

    /**
     * 格式化日期为字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.format(date);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetSoldVO getSoldByAssetId(String assetId) {
        if (!StringUtils.hasText(assetId)) {
            throw new BusinessException("400", "资产ID不能为空");
        }

        String userId = getCurrentUserId();

        // 验证资产是否存在且属于当前用户
        AssetPO asset = assetMapper.selectById(assetId, userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        AssetSoldPO sold = soldMapper.selectByAssetId(assetId);
        if (sold == null) {
            return null;
        }

        return convertToVO(sold);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetSoldVO createSold(AssetSoldCreateDTO createDTO) {
        // 参数校验
        if (createDTO == null || !StringUtils.hasText(createDTO.getAssetId())) {
            throw new BusinessException("400", "资产ID不能为空");
        }
        if (createDTO.getSoldPrice() == null || createDTO.getSoldPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "卖出价格必须大于0");
        }
        if (!StringUtils.hasText(createDTO.getSoldDate())) {
            throw new BusinessException("400", "卖出日期不能为空");
        }

        String userId = getCurrentUserId();

        // 验证资产是否存在且属于当前用户
        AssetPO asset = assetMapper.selectById(createDTO.getAssetId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 检查是否已有卖出记录
        AssetSoldPO existingSold = soldMapper.selectByAssetId(createDTO.getAssetId());
        if (existingSold != null) {
            throw new BusinessException("400", "该资产已有卖出记录");
        }

        // 创建卖出记录
        AssetSoldPO newSold = new AssetSoldPO();
        newSold.setId(idGeneratorApi.generateId());
        newSold.setAssetId(createDTO.getAssetId());
        newSold.setSoldPrice(createDTO.getSoldPrice());
        newSold.setSoldDate(parseDate(createDTO.getSoldDate()));
        newSold.setSoldReason(createDTO.getSoldReason());
        newSold.setRemark(createDTO.getRemark());
        Date now = new Date();
        newSold.setCreatedAt(now);
        newSold.setUpdatedAt(now);

        int insertCount = soldMapper.insertSold(newSold);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建卖出记录失败");
        }

        // 更新资产状态为已卖出
        asset.setStatus("SOLD");
        asset.setInService(false);
        asset.setVersion(asset.getVersion() + 1);
        asset.setUpdatedAt(new Date());
        assetMapper.updateAsset(asset);

        AssetSoldPO insertedSold = soldMapper.selectById(newSold.getId());
        return convertToVO(insertedSold);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetSoldVO updateSold(AssetSoldUpdateDTO updateDTO) {
        // 参数校验
        if (updateDTO == null || !StringUtils.hasText(updateDTO.getId())) {
            throw new BusinessException("400", "卖出记录ID不能为空");
        }
        if (updateDTO.getSoldPrice() == null || updateDTO.getSoldPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "卖出价格必须大于0");
        }
        if (!StringUtils.hasText(updateDTO.getSoldDate())) {
            throw new BusinessException("400", "卖出日期不能为空");
        }

        String userId = getCurrentUserId();

        // 查询卖出记录
        AssetSoldPO sold = soldMapper.selectById(updateDTO.getId());
        if (sold == null) {
            throw new BusinessException("404", "卖出记录不存在");
        }

        // 验证资产是否属于当前用户
        AssetPO asset = assetMapper.selectById(sold.getAssetId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 更新卖出记录
        sold.setSoldPrice(updateDTO.getSoldPrice());
        sold.setSoldDate(parseDate(updateDTO.getSoldDate()));
        sold.setSoldReason(updateDTO.getSoldReason());
        sold.setRemark(updateDTO.getRemark());
        sold.setUpdatedAt(new Date());

        int updateCount = soldMapper.updateSold(sold);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新卖出记录失败");
        }

        AssetSoldPO updatedSold = soldMapper.selectById(updateDTO.getId());
        return convertToVO(updatedSold);
    }

    /**
     * 转换为VO
     */
    private AssetSoldVO convertToVO(AssetSoldPO po) {
        AssetSoldVO vo = new AssetSoldVO();
        vo.setId(po.getId());
        vo.setSoldPrice(po.getSoldPrice());
        vo.setSoldDate(formatDate(po.getSoldDate()));
        vo.setSoldReason(po.getSoldReason());
        vo.setRemark(po.getRemark());
        return vo;
    }
}

