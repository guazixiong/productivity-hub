package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.AssetAdditionalFeeCreateDTO;
import com.pbad.asset.domain.dto.AssetAdditionalFeeUpdateDTO;
import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.vo.AssetAdditionalFeeVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.service.AssetAdditionalFeeService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附加费用服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class AssetAdditionalFeeServiceImpl implements AssetAdditionalFeeService {

    private final AssetAdditionalFeeMapper feeMapper;
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
    public List<AssetAdditionalFeeVO> getFeesByAssetId(String assetId) {
        if (!StringUtils.hasText(assetId)) {
            throw new BusinessException("400", "资产ID不能为空");
        }

        String userId = getCurrentUserId();

        // 验证资产是否存在且属于当前用户
        AssetPO asset = assetMapper.selectById(assetId, userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        List<AssetAdditionalFeePO> fees = feeMapper.selectByAssetId(assetId);
        return fees.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetAdditionalFeeVO createFee(AssetAdditionalFeeCreateDTO createDTO) {
        // 参数校验
        if (createDTO == null || !StringUtils.hasText(createDTO.getAssetId())) {
            throw new BusinessException("400", "资产ID不能为空");
        }
        if (!StringUtils.hasText(createDTO.getName())) {
            throw new BusinessException("400", "费用名称不能为空");
        }
        if (createDTO.getAmount() == null || createDTO.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "费用金额必须大于0");
        }
        if (!StringUtils.hasText(createDTO.getFeeDate())) {
            throw new BusinessException("400", "费用日期不能为空");
        }

        String userId = getCurrentUserId();
        String assetId = createDTO.getAssetId();

        // 验证资产是否存在且属于当前用户
        AssetPO asset = assetMapper.selectById(assetId, userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 创建附加费用
        AssetAdditionalFeePO newFee = new AssetAdditionalFeePO();
        newFee.setId(idGeneratorApi.generateId());
        newFee.setAssetId(createDTO.getAssetId());
        newFee.setName(createDTO.getName().trim());
        newFee.setAmount(createDTO.getAmount());
        newFee.setFeeDate(parseDate(createDTO.getFeeDate()));
        newFee.setRemark(createDTO.getRemark());
        Date now = new Date();
        newFee.setCreatedAt(now);
        newFee.setUpdatedAt(now);

        int insertCount = feeMapper.insertFee(newFee);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建附加费用失败");
        }

        AssetAdditionalFeePO insertedFee = feeMapper.selectById(newFee.getId());
        return convertToVO(insertedFee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetAdditionalFeeVO updateFee(AssetAdditionalFeeUpdateDTO updateDTO) {
        // 参数校验
        if (updateDTO == null || !StringUtils.hasText(updateDTO.getId())) {
            throw new BusinessException("400", "附加费用ID不能为空");
        }
        if (!StringUtils.hasText(updateDTO.getName())) {
            throw new BusinessException("400", "费用名称不能为空");
        }
        if (updateDTO.getAmount() == null || updateDTO.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "费用金额必须大于0");
        }
        if (!StringUtils.hasText(updateDTO.getFeeDate())) {
            throw new BusinessException("400", "费用日期不能为空");
        }

        String userId = getCurrentUserId();

        // 查询附加费用
        AssetAdditionalFeePO fee = feeMapper.selectById(updateDTO.getId());
        if (fee == null) {
            throw new BusinessException("404", "附加费用不存在");
        }

        // 验证资产是否属于当前用户
        AssetPO asset = assetMapper.selectById(fee.getAssetId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 更新附加费用
        fee.setName(updateDTO.getName().trim());
        fee.setAmount(updateDTO.getAmount());
        fee.setFeeDate(parseDate(updateDTO.getFeeDate()));
        fee.setRemark(updateDTO.getRemark());
        fee.setUpdatedAt(new Date());

        int updateCount = feeMapper.updateFee(fee);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新附加费用失败");
        }

        AssetAdditionalFeePO updatedFee = feeMapper.selectById(updateDTO.getId());
        return convertToVO(updatedFee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFee(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "附加费用ID不能为空");
        }

        String userId = getCurrentUserId();

        // 查询附加费用
        AssetAdditionalFeePO fee = feeMapper.selectById(id);
        if (fee == null) {
            throw new BusinessException("404", "附加费用不存在");
        }

        // 验证资产是否属于当前用户
        AssetPO asset = assetMapper.selectById(fee.getAssetId(), userId);
        if (asset == null || Boolean.TRUE.equals(asset.getDeleted())) {
            throw new BusinessException("404", "资产不存在");
        }

        // 删除附加费用
        int deleteCount = feeMapper.deleteFee(id);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除附加费用失败");
        }
    }

    /**
     * 转换为VO
     */
    private AssetAdditionalFeeVO convertToVO(AssetAdditionalFeePO po) {
        AssetAdditionalFeeVO vo = new AssetAdditionalFeeVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setAmount(po.getAmount());
        vo.setFeeDate(formatDate(po.getFeeDate()));
        vo.setRemark(po.getRemark());
        return vo;
    }
}

