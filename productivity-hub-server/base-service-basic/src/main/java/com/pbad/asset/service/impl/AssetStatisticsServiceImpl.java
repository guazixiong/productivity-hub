package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.vo.AssetCardStatisticsVO;
import com.pbad.asset.domain.vo.AssetDistributionVO;
import com.pbad.asset.domain.vo.AssetStatisticsVO;
import com.pbad.asset.domain.vo.CategoryStatisticsVO;
import com.pbad.asset.domain.vo.StatisticsOverviewVO;
import com.pbad.asset.domain.vo.StatisticsTrendVO;
import com.pbad.asset.domain.vo.WishlistCardStatisticsVO;
import com.pbad.asset.domain.vo.WishlistVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.service.AssetStatisticsService;
import com.pbad.asset.statistics.factory.ChartDataGeneratorFactory;
import com.pbad.asset.statistics.factory.ChartType;
import com.pbad.asset.strategy.DepreciationContext;
import com.pbad.asset.service.WishlistService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产统计服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class AssetStatisticsServiceImpl implements AssetStatisticsService {

    private final AssetMapper assetMapper;
    private final AssetAdditionalFeeMapper additionalFeeMapper;
    private final ChartDataGeneratorFactory chartDataGeneratorFactory;
    private final DepreciationContext depreciationContext;
    private final WishlistService wishlistService;
    
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

    @Override
    @Transactional(readOnly = true)
    public AssetStatisticsVO getStatistics() {
        String userId = getCurrentUserId();

        // 获取所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);

        AssetStatisticsVO statistics = new AssetStatisticsVO();

        // 统计资产总数
        statistics.setTotalAssets(assets.size());

        // 统计各状态资产数量
        long inServiceCount = assets.stream()
                .filter(asset -> "IN_SERVICE".equals(asset.getStatus()))
                .count();
        statistics.setInServiceAssets((int) inServiceCount);

        long retiredCount = assets.stream()
                .filter(asset -> "RETIRED".equals(asset.getStatus()))
                .count();
        statistics.setRetiredAssets((int) retiredCount);

        // 计算总价值
        BigDecimal totalValue = BigDecimal.ZERO;
        for (AssetPO asset : assets) {
            if (asset.getPrice() != null) {
                totalValue = totalValue.add(asset.getPrice());
            }

            // 累加附加费用
            List<com.pbad.asset.domain.po.AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            for (com.pbad.asset.domain.po.AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    totalValue = totalValue.add(fee.getAmount());
                }
            }
        }
        statistics.setTotalValue(totalValue);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsOverviewVO getOverview(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        
        // 获取所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);
        
        // 计算日期范围
        Date[] dateRange = calculateDateRange(query);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        
        StatisticsOverviewVO overview = new StatisticsOverviewVO();
        
        // 计算购入金额和件数（在指定期间内购买的资产）
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        int purchaseCount = 0;
        for (AssetPO asset : assets) {
            if (asset.getPurchaseDate() != null && isDateInRange(asset.getPurchaseDate(), startDate, endDate)) {
                if (asset.getPrice() != null) {
                    purchaseAmount = purchaseAmount.add(asset.getPrice());
                }
                // 累加附加费用
                List<com.pbad.asset.domain.po.AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
                for (com.pbad.asset.domain.po.AssetAdditionalFeePO fee : fees) {
                    if (fee.getAmount() != null) {
                        purchaseAmount = purchaseAmount.add(fee.getAmount());
                    }
                }
                purchaseCount++;
            }
        }
        overview.setPurchaseAmount(purchaseAmount);
        overview.setPurchaseCount(purchaseCount);
        
        // ==================== 日均总额计算 ====================
        // 计算公式：日均总额 = SUM((每件资产价值 + 额外费用) / 使用天数)
        // 说明：
        // 1. 每件资产价值 = 资产购入价格 + 该资产的所有附加费用
        // 2. 使用天数 = 根据资产的贬值策略计算（按使用日期或按使用次数）
        // 3. 日均总额表示：所有资产的日均价格总和
        // ====================================================
        BigDecimal totalDailyAverage = BigDecimal.ZERO;
        for (AssetPO asset : assets) {
            // 获取附加费用总额
            List<com.pbad.asset.domain.po.AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            BigDecimal additionalFeesTotal = BigDecimal.ZERO;
            for (com.pbad.asset.domain.po.AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    additionalFeesTotal = additionalFeesTotal.add(fee.getAmount());
                }
            }
            
            // 使用策略模式计算每个资产的日均价格
            BigDecimal dailyAverage = depreciationContext.calculate(asset, additionalFeesTotal);
            totalDailyAverage = totalDailyAverage.add(dailyAverage);
        }
        overview.setTotalDailyAverage(totalDailyAverage);
        
        return overview;
    }
    
    /**
     * 计算日期范围
     */
    private Date[] calculateDateRange(StatisticsQueryDTO query) {
        Date startDate;
        Date endDate = new Date();
        
        if (query != null && query.getStartDate() != null && query.getEndDate() != null) {
            try {
                startDate = DATE_FORMAT.parse(query.getStartDate());
                endDate = DATE_FORMAT.parse(query.getEndDate());
            } catch (ParseException e) {
                throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
            }
        } else {
            // 根据period计算日期范围
            String period = query != null && query.getPeriod() != null ? query.getPeriod() : "WEEK";
            LocalDate today = LocalDate.now();
            LocalDate start;
            
            switch (period.toUpperCase()) {
                case "MONTH":
                    start = today.minusDays(29);
                    break;
                case "YEAR":
                    start = today.minusDays(364);
                    break;
                case "ALL":
                    // 查询所有资产的最早购买日期
                    start = null;
                    break;
                case "WEEK":
                default:
                    start = today.minusDays(6);
                    break;
            }
            
            if (start != null) {
                startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                // 查询所有资产的最早购买日期
                List<AssetPO> allAssets = assetMapper.selectAllByUserId(getCurrentUserId());
                if (allAssets.isEmpty()) {
                    startDate = endDate;
                } else {
                    Date earliestDate = allAssets.stream()
                            .filter(asset -> asset.getPurchaseDate() != null)
                            .map(AssetPO::getPurchaseDate)
                            .min(Date::compareTo)
                            .orElse(endDate);
                    startDate = earliestDate;
                }
            }
        }
        
        return new Date[]{startDate, endDate};
    }
    
    /**
     * 判断日期是否在范围内
     */
    private boolean isDateInRange(Date date, Date startDate, Date endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.before(startDate) && !date.after(endDate);
    }
    

    @Override
    @Transactional(readOnly = true)
    public StatisticsTrendVO getDailyAverageTrend(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        return chartDataGeneratorFactory.generate(ChartType.DAILY_AVERAGE_TREND, userId, query);
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsTrendVO getTotalValueTrend(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        return chartDataGeneratorFactory.generate(ChartType.TOTAL_VALUE_TREND, userId, query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDistributionVO> getAssetDistribution(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        return chartDataGeneratorFactory.generate(ChartType.ASSET_DISTRIBUTION, userId, query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatisticsVO> getCategoryStatistics(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        return chartDataGeneratorFactory.generate(ChartType.CATEGORY_STATISTICS, userId, query);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetCardStatisticsVO getAssetCardStatistics(StatisticsQueryDTO query) {
        String userId = getCurrentUserId();
        
        // 获取所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);
        
        AssetCardStatisticsVO statistics = new AssetCardStatisticsVO();
        
        // 资产总数
        statistics.setTotalCount(assets.size());
        
        // 资产价格之和（过滤已退役资产）
        BigDecimal totalPrice = BigDecimal.ZERO;
        // 总资产附加费用之和（所有资产）
        BigDecimal totalAdditionalFees = BigDecimal.ZERO;
        // 日均总额
        BigDecimal dailyAverageTotal = BigDecimal.ZERO;
        
        for (AssetPO asset : assets) {
            // 获取附加费用总额
            List<com.pbad.asset.domain.po.AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            BigDecimal additionalFeesTotal = BigDecimal.ZERO;
            for (com.pbad.asset.domain.po.AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    additionalFeesTotal = additionalFeesTotal.add(fee.getAmount());
                }
            }
            
            // 累加所有资产的附加费用
            totalAdditionalFees = totalAdditionalFees.add(additionalFeesTotal);
            
            // 只统计非退役资产的价格
            if (!"RETIRED".equals(asset.getStatus()) && asset.getPrice() != null) {
                totalPrice = totalPrice.add(asset.getPrice());
            }
            
            // 计算日均总额（所有资产，包括已退役的）
            BigDecimal dailyAverage = depreciationContext.calculate(asset, additionalFeesTotal);
            dailyAverageTotal = dailyAverageTotal.add(dailyAverage);
        }
        
        statistics.setTotalPrice(totalPrice);
        statistics.setTotalAdditionalFees(totalAdditionalFees);
        // 资产总价格 = 资产价格之和（非退役）+ 资产附加费用之和
        statistics.setTotalValue(totalPrice.add(totalAdditionalFees));
        statistics.setDailyAverageTotal(dailyAverageTotal);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistCardStatisticsVO getWishlistCardStatistics(StatisticsQueryDTO query) {
        // 依赖 WishlistService 内部的用户上下文校验
        List<WishlistVO> wishlists = wishlistService.getWishlistList(null);

        WishlistCardStatisticsVO statistics = new WishlistCardStatisticsVO();
        statistics.setTotalCount(wishlists.size());

        BigDecimal totalValue = BigDecimal.ZERO;
        for (WishlistVO item : wishlists) {
            if (item.getPrice() != null) {
                totalValue = totalValue.add(item.getPrice());
            }
        }
        statistics.setTotalValue(totalValue);

        Date[] dateRange = calculateDateRange(query);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        int addedCount = 0;
        int achievedCount = 0;

        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault())
                    .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

            for (WishlistVO item : wishlists) {
                Date createdAt = item.getCreatedAt();
                if (createdAt != null) {
                    LocalDateTime created = LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault());
                    if (!created.isBefore(start) && !created.isAfter(end)) {
                        addedCount++;
                    }
                }

                if (Boolean.TRUE.equals(item.getAchieved())) {
                    Date achievedAt = item.getAchievedAt();
                    if (achievedAt != null) {
                        LocalDateTime achieved = LocalDateTime.ofInstant(achievedAt.toInstant(), ZoneId.systemDefault());
                        if (!achieved.isBefore(start) && !achieved.isAfter(end)) {
                            achievedCount++;
                        }
                    }
                }
            }
        } else {
            // ALL 情况：全部计入新增，已实现计入实现数
            addedCount = wishlists.size();
            achievedCount = (int) wishlists.stream()
                    .filter(w -> Boolean.TRUE.equals(w.getAchieved()))
                    .count();
        }

        statistics.setAddedCount(addedCount);
        statistics.setAchievedCount(achievedCount);

        return statistics;
    }

}

