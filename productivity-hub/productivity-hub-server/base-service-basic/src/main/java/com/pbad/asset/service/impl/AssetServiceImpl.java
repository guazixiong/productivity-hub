package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.*;
import com.pbad.asset.domain.enums.AssetEventType;
import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.po.AssetSoldPO;
import com.pbad.asset.domain.vo.AssetAdditionalFeeVO;
import com.pbad.asset.domain.vo.AssetDetailVO;
import com.pbad.asset.domain.vo.AssetListVO;
import com.pbad.asset.domain.vo.AssetPageVO;
import com.pbad.asset.domain.vo.AssetSoldVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.AssetSoldMapper;
import com.pbad.asset.observer.AssetEventPublisher;
import com.pbad.asset.service.AssetService;
import com.pbad.asset.state.AssetStateContext;
import com.pbad.asset.strategy.DepreciationContext;
import com.pbad.asset.validator.ValidationResult;
import com.pbad.asset.validator.Validator;
import com.pbad.asset.validator.ValidatorChainBuilder;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper categoryMapper;
    private final AssetAdditionalFeeMapper additionalFeeMapper;
    private final AssetSoldMapper soldMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final DepreciationContext depreciationContext;
    private final AssetStateContext assetStateContext;
    private final ValidatorChainBuilder validatorChainBuilder;
    private final AssetEventPublisher assetEventPublisher;

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
    public AssetPageVO getAssetPage(AssetQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new AssetQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        String userId = getCurrentUserId();
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        List<AssetPO> assets = assetMapper.selectPage(
                userId,
                queryDTO.getCategoryId(),
                queryDTO.getStatus(),
                offset,
                queryDTO.getPageSize()
        );
        long total = assetMapper.count(userId, queryDTO.getCategoryId(), queryDTO.getStatus());

        List<AssetListVO> voList = assets.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        AssetPageVO pageVO = new AssetPageVO();
        pageVO.setList(voList);
        pageVO.setTotal(total);
        pageVO.setPageNum(queryDTO.getPageNum());
        pageVO.setPageSize(queryDTO.getPageSize());

        return pageVO;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDetailVO getAssetById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "资产ID不能为空");
        }
        String userId = getCurrentUserId();
        AssetPO asset = assetMapper.selectById(id, userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }
        return convertToDetailVO(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDetailVO createAsset(AssetCreateDTO createDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildCreateChain();
        ValidationResult validationResult = validator.validate(createDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException("400", validationResult.getErrorMessage());
        }

        String userId = getCurrentUserId();

        // 创建资产
        AssetPO newAsset = new AssetPO();
        newAsset.setId(idGeneratorApi.generateId());
        newAsset.setUserId(userId);
        newAsset.setCategoryId(createDTO.getCategoryId());
        newAsset.setName(createDTO.getName().trim());
        newAsset.setPrice(createDTO.getPrice());
        newAsset.setImage(createDTO.getImage());
        newAsset.setRemark(createDTO.getRemark());
        newAsset.setPurchaseDate(parseDate(createDTO.getPurchaseDate()));
        newAsset.setWarrantyEnabled(createDTO.getWarrantyEnabled());
        if (createDTO.getWarrantyEnabled() != null && createDTO.getWarrantyEnabled()) {
            newAsset.setWarrantyEndDate(parseDate(createDTO.getWarrantyEndDate()));
        }
        newAsset.setDepreciationByUsageCount(createDTO.getDepreciationByUsageCount());
        newAsset.setExpectedUsageCount(createDTO.getExpectedUsageCount());
        newAsset.setDepreciationByUsageDate(createDTO.getDepreciationByUsageDate());
        newAsset.setUsageDate(parseDate(createDTO.getUsageDate()));
        newAsset.setInService(createDTO.getInService() != null ? createDTO.getInService() : true);
        newAsset.setRetiredDate(parseDate(createDTO.getRetiredDate()));
        
        // 设置状态
        if (newAsset.getInService() != null && newAsset.getInService()) {
            newAsset.setStatus("IN_SERVICE");
        } else if (newAsset.getRetiredDate() != null) {
            newAsset.setStatus("RETIRED");
        } else {
            newAsset.setStatus("IN_SERVICE");
        }
        
        newAsset.setVersion(1);
        newAsset.setDeleted(false);
        Date now = new Date();
        newAsset.setCreatedAt(now);
        newAsset.setUpdatedAt(now);

        int insertCount = assetMapper.insertAsset(newAsset);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建资产失败");
        }

        AssetPO insertedAsset = assetMapper.selectById(newAsset.getId(), userId);

        // 发布资产创建事件
        assetEventPublisher.publish(AssetEventType.CREATED, insertedAsset);

        return convertToDetailVO(insertedAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDetailVO updateAsset(AssetUpdateDTO updateDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildUpdateChain();
        ValidationResult validationResult = validator.validate(updateDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException("400", validationResult.getErrorMessage());
        }

        String userId = getCurrentUserId();

        // 查询资产
        AssetPO asset = assetMapper.selectById(updateDTO.getId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 使用状态模式检查是否可以编辑
        if (!assetStateContext.canEdit(asset)) {
            throw new BusinessException("400", "当前状态不允许编辑操作");
        }

        // 乐观锁检查 - 已移除
        // if (!asset.getVersion().equals(updateDTO.getVersion())) {
        //     throw new BusinessException("409", "资产已被其他用户修改，请刷新后重试");
        // }

        // 更新资产
        if (StringUtils.hasText(updateDTO.getName())) {
            asset.setName(updateDTO.getName().trim());
        }
        if (updateDTO.getPrice() != null) {
            asset.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getImage() != null) {
            asset.setImage(updateDTO.getImage());
        }
        if (updateDTO.getRemark() != null) {
            asset.setRemark(updateDTO.getRemark());
        }
        if (StringUtils.hasText(updateDTO.getCategoryId())) {
            asset.setCategoryId(updateDTO.getCategoryId());
        }
        if (StringUtils.hasText(updateDTO.getPurchaseDate())) {
            asset.setPurchaseDate(parseDate(updateDTO.getPurchaseDate()));
        }
        if (updateDTO.getWarrantyEnabled() != null) {
            asset.setWarrantyEnabled(updateDTO.getWarrantyEnabled());
            if (updateDTO.getWarrantyEnabled()) {
                asset.setWarrantyEndDate(parseDate(updateDTO.getWarrantyEndDate()));
            } else {
                asset.setWarrantyEndDate(null);
            }
        }
        if (updateDTO.getDepreciationByUsageCount() != null) {
            asset.setDepreciationByUsageCount(updateDTO.getDepreciationByUsageCount());
            asset.setExpectedUsageCount(updateDTO.getExpectedUsageCount());
        }
        if (updateDTO.getDepreciationByUsageDate() != null) {
            asset.setDepreciationByUsageDate(updateDTO.getDepreciationByUsageDate());
            asset.setUsageDate(parseDate(updateDTO.getUsageDate()));
        }
        if (updateDTO.getInService() != null) {
            asset.setInService(updateDTO.getInService());
        }
        if (StringUtils.hasText(updateDTO.getRetiredDate())) {
            asset.setRetiredDate(parseDate(updateDTO.getRetiredDate()));
        }

        // 更新状态
        if (asset.getInService() != null && asset.getInService()) {
            asset.setStatus("IN_SERVICE");
        } else if (asset.getRetiredDate() != null) {
            asset.setStatus("RETIRED");
        } else if ("SOLD".equals(asset.getStatus())) {
            // 已卖出状态保持不变
        } else {
            asset.setStatus("IN_SERVICE");
        }

        asset.setVersion(asset.getVersion() + 1);
        asset.setUpdatedAt(new Date());

        int updateCount = assetMapper.updateAsset(asset);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新资产失败");
        }

        AssetPO updatedAsset = assetMapper.selectById(updateDTO.getId(), userId);

        // 发布资产更新事件
        assetEventPublisher.publish(AssetEventType.UPDATED, updatedAsset);

        return convertToDetailVO(updatedAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "资产ID不能为空");
        }

        String userId = getCurrentUserId();

        // 查询资产
        AssetPO asset = assetMapper.selectById(id, userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 软删除资产
        int deleteCount = assetMapper.deleteAsset(id, userId);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除资产失败");
        }

        // 发布资产删除事件（使用原始资产信息）
        assetEventPublisher.publish(AssetEventType.DELETED, asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAssets(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("400", "资产ID列表不能为空");
        }

        String userId = getCurrentUserId();

        // 批量软删除资产
        int deleteCount = assetMapper.batchDeleteAssets(ids, userId);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "批量删除资产失败");
        }

        // 为了避免额外查询，这里不逐条发布事件，如后续有需要可扩展为按ID查询并批量发布
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssetStatus(AssetStatusUpdateDTO updateStatusDTO) {
        // 参数校验
        if (updateStatusDTO == null || !StringUtils.hasText(updateStatusDTO.getId())) {
            throw new BusinessException("400", "资产ID不能为空");
        }
        if (!StringUtils.hasText(updateStatusDTO.getStatus())) {
            throw new BusinessException("400", "状态不能为空");
        }
        if (updateStatusDTO.getVersion() == null) {
            throw new BusinessException("400", "版本号不能为空");
        }

        String status = updateStatusDTO.getStatus();
        if (!"IN_SERVICE".equals(status) && !"RETIRED".equals(status) && !"SOLD".equals(status)) {
            throw new BusinessException("400", "状态值无效");
        }

        String userId = getCurrentUserId();

        // 查询资产
        AssetPO asset = assetMapper.selectById(updateStatusDTO.getId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 乐观锁检查 - 已移除
        // if (!asset.getVersion().equals(updateStatusDTO.getVersion())) {
        //     throw new BusinessException("409", "资产已被其他用户修改，请刷新后重试");
        // }

        // 使用状态模式更新状态
        AssetEventType eventType = null;
        if ("RETIRED".equals(status)) {
            // 检查是否可以退役
            if (!assetStateContext.canRetire(asset)) {
                throw new BusinessException("400", "当前状态不允许退役操作");
            }
            assetStateContext.retire(asset);
            eventType = AssetEventType.RETIRED;
        } else if ("SOLD".equals(status)) {
            // 检查是否可以卖出
            if (!assetStateContext.canSell(asset)) {
                throw new BusinessException("400", "当前状态不允许卖出操作");
            }
            assetStateContext.sell(asset);
            eventType = AssetEventType.SOLD;
        } else if ("IN_SERVICE".equals(status)) {
            // 恢复为正在服役状态
            asset.setStatus("IN_SERVICE");
            asset.setInService(true);
            asset.setRetiredDate(null);
            eventType = AssetEventType.UPDATED;
        }

        asset.setVersion(asset.getVersion() + 1);
        asset.setUpdatedAt(new Date());

        int updateCount = assetMapper.updateAsset(asset);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新资产状态失败");
        }

        // 发布状态变更相关的资产事件
        if (eventType != null) {
            assetEventPublisher.publish(eventType, asset);
        }
    }

    /**
     * 转换为列表VO
     */
    private AssetListVO convertToListVO(AssetPO po) {
        AssetListVO vo = new AssetListVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setPrice(po.getPrice());
        vo.setImage(po.getImage());
        vo.setCategoryId(po.getCategoryId());
        vo.setStatus(po.getStatus());
        vo.setPurchaseDate(formatDate(po.getPurchaseDate()));

        // 获取分类名称
        if (StringUtils.hasText(po.getCategoryId())) {
            AssetCategoryPO category = categoryMapper.selectById(po.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 计算总价值和日均价格
        calculateAssetValue(po, vo);

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private AssetDetailVO convertToDetailVO(AssetPO po) {
        AssetDetailVO vo = new AssetDetailVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setPrice(po.getPrice());
        vo.setImage(po.getImage());
        vo.setRemark(po.getRemark());
        vo.setCategoryId(po.getCategoryId());
        vo.setPurchaseDate(formatDate(po.getPurchaseDate()));
        vo.setWarrantyEnabled(po.getWarrantyEnabled());
        vo.setWarrantyEndDate(formatDate(po.getWarrantyEndDate()));
        vo.setDepreciationByUsageCount(po.getDepreciationByUsageCount());
        vo.setExpectedUsageCount(po.getExpectedUsageCount());
        vo.setDepreciationByUsageDate(po.getDepreciationByUsageDate());
        vo.setUsageDate(formatDate(po.getUsageDate()));
        vo.setInService(po.getInService());
        vo.setRetiredDate(formatDate(po.getRetiredDate()));
        vo.setStatus(po.getStatus());

        // 获取分类名称
        if (StringUtils.hasText(po.getCategoryId())) {
            AssetCategoryPO category = categoryMapper.selectById(po.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 获取附加费用列表
        List<AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(po.getId());
        List<AssetAdditionalFeeVO> feeVOs = fees.stream()
                .map(this::convertFeeToVO)
                .collect(Collectors.toList());
        vo.setAdditionalFees(feeVOs);

        // 获取卖出信息
        AssetSoldPO sold = soldMapper.selectByAssetId(po.getId());
        if (sold != null) {
            vo.setSoldInfo(convertSoldToVO(sold));
        }

        // 计算总价值、使用天数、日均价格
        calculateAssetDetailValue(po, vo, fees);

        return vo;
    }

    /**
     * 计算资产总价值和日均价格（列表用）
     */
    private void calculateAssetValue(AssetPO po, AssetListVO vo) {
        BigDecimal totalValue = po.getPrice();
        
        // 累加附加费用
        List<AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(po.getId());
        BigDecimal additionalFeesTotal = BigDecimal.ZERO;
        for (AssetAdditionalFeePO fee : fees) {
            if (fee.getAmount() != null) {
                additionalFeesTotal = additionalFeesTotal.add(fee.getAmount());
                totalValue = totalValue.add(fee.getAmount());
            }
        }
        
        vo.setTotalValue(totalValue);
        vo.setAdditionalFeesTotal(additionalFeesTotal);

        // 已退役的资产，日均金额为0
        if ("RETIRED".equals(po.getStatus())) {
            vo.setDailyAveragePrice(BigDecimal.ZERO);
        } else {
            // 使用策略模式计算日均价格
            BigDecimal dailyAverage = depreciationContext.calculate(po, additionalFeesTotal);
            vo.setDailyAveragePrice(dailyAverage);
        }
    }

    /**
     * 计算资产总价值、使用天数、日均价格（详情用）
     */
    private void calculateAssetDetailValue(AssetPO po, AssetDetailVO vo, List<AssetAdditionalFeePO> fees) {
        BigDecimal totalValue = po.getPrice();
        
        // 累加附加费用
        BigDecimal additionalFeesTotal = BigDecimal.ZERO;
        for (AssetAdditionalFeePO fee : fees) {
            if (fee.getAmount() != null) {
                additionalFeesTotal = additionalFeesTotal.add(fee.getAmount());
                totalValue = totalValue.add(fee.getAmount());
            }
        }
        
        vo.setTotalValue(totalValue);

        // 计算使用天数（用于显示）：当前时间 - 购入时间 + 1
        if (po.getPurchaseDate() != null) {
            Date endDate = po.getRetiredDate() != null ? po.getRetiredDate() : new Date();
            long days = (endDate.getTime() - po.getPurchaseDate().getTime()) / (1000 * 60 * 60 * 24) + 1;
            vo.setUsageDays((int) days);
        }
        
        // 已退役的资产，日均金额为0
        if ("RETIRED".equals(po.getStatus())) {
            vo.setDailyAveragePrice(BigDecimal.ZERO);
        } else {
            // 使用策略模式计算日均价格
            BigDecimal dailyAverage = depreciationContext.calculate(po, additionalFeesTotal);
            vo.setDailyAveragePrice(dailyAverage);
        }
    }

    /**
     * 转换附加费用为VO
     */
    private AssetAdditionalFeeVO convertFeeToVO(AssetAdditionalFeePO po) {
        AssetAdditionalFeeVO vo = new AssetAdditionalFeeVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setAmount(po.getAmount());
        vo.setFeeDate(formatDate(po.getFeeDate()));
        vo.setRemark(po.getRemark());
        return vo;
    }

    /**
     * 转换卖出信息为VO
     */
    private AssetSoldVO convertSoldToVO(AssetSoldPO po) {
        AssetSoldVO vo = new AssetSoldVO();
        vo.setId(po.getId());
        vo.setSoldPrice(po.getSoldPrice());
        vo.setSoldDate(formatDate(po.getSoldDate()));
        vo.setSoldReason(po.getSoldReason());
        vo.setRemark(po.getRemark());
        return vo;
    }
}

