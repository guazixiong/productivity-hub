package com.pbad.tools.schedule;

import com.pbad.config.service.ConfigService;
import com.pbad.tools.domain.po.ToolStatPO;
import com.pbad.tools.mapper.ToolStatMapper;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 工具统计同步定时任务.
 * <p>
 * 每小时将Redis中的工具统计数据同步到数据库.
 * </p>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolStatSyncTask {

    private static final String REDIS_KEY_PREFIX = "tool:stat:";

    private final RedisUtil redisUtil;
    private final ToolStatMapper toolStatMapper;
    private final ConfigService configService;

    /**
     * 每小时执行一次，将Redis热点数据落库.
     * <p>
     * Cron表达式: 0 0 * * * ? (每小时的第0分0秒执行)
     * </p>
     */
    @Scheduled(cron = "0 0 * * * ?", zone = "Asia/Shanghai")
    @Transactional(rollbackFor = Exception.class)
    public void syncToolStatsToDatabase() {
        if (!isTaskEnabled("toolStatSync.enabled")) {
            log.info("工具统计数据同步任务已被关闭，跳过执行");
            return;
        }
        log.info("开始执行工具统计数据同步任务：将Redis热点数据落库");
        try {
            // 获取所有Redis中的工具统计key
            Set<String> redisKeys = redisUtil.keys(REDIS_KEY_PREFIX + "*");
            if (redisKeys == null || redisKeys.isEmpty()) {
                log.info("Redis中没有工具统计数据，跳过同步");
                return;
            }

            List<ToolStatPO> toolStatsToSync = new ArrayList<>();
            int syncedCount = 0;
            int deletedCount = 0;

            for (String redisKey : redisKeys) {
                try {
                    String toolId = redisKey.substring(REDIS_KEY_PREFIX.length());
                    Object clicksObj = redisUtil.getValue(redisKey);
                    if (clicksObj == null) {
                        continue;
                    }

                    Integer clicks = parseClicks(clicksObj);
                    if (clicks == null || clicks <= 0) {
                        // 跳过无效数据
                        continue;
                    }

                    // 查询数据库中是否已存在
                    ToolStatPO existingStat = toolStatMapper.selectById(toolId);
                    if (existingStat != null) {
                        // 更新：将Redis增量累加到数据库
                        existingStat.setClicks(existingStat.getClicks() + clicks);
                        toolStatsToSync.add(existingStat);
                    } else {
                        // 新增：创建新记录
                        ToolStatPO newStat = new ToolStatPO();
                        newStat.setId(toolId);
                        newStat.setToolName(toolId); // 默认使用toolId作为名称
                        newStat.setClicks(clicks);
                        toolStatsToSync.add(newStat);
                    }

                    // 删除Redis中的key（因为已经落库）
                    redisUtil.delete(redisKey);
                    deletedCount++;

                } catch (Exception e) {
                    log.warn("处理Redis key {} 时发生错误: {}", redisKey, e.getMessage());
                }
            }

            // 批量更新或插入到数据库
            if (!toolStatsToSync.isEmpty()) {
                int batchSize = 100; // 每批处理100条
                for (int i = 0; i < toolStatsToSync.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toolStatsToSync.size());
                    List<ToolStatPO> batch = toolStatsToSync.subList(i, end);
                    toolStatMapper.batchUpsert(batch);
                    syncedCount += batch.size();
                }
                log.info("成功同步 {} 条工具统计数据到数据库，删除 {} 个Redis key", syncedCount, deletedCount);
            } else {
                log.info("没有需要同步的数据");
            }

            // 清除统计列表缓存，下次查询时重新构建
            redisUtil.delete("tool:stats:list");

        } catch (Exception e) {
            log.error("工具统计数据同步失败: {}", e.getMessage(), e);
        }
    }

    private boolean isTaskEnabled(String key) {
        try {
            String value = configService.getTemplateConfigValue("schedule", key);
            return !"false".equalsIgnoreCase(value) && !"0".equals(value);
        } catch (Exception ex) {
            // 如果配置不存在或读取失败，默认视为开启
            return true;
        }
    }

    /**
     * 解析点击次数
     */
    private Integer parseClicks(Object clicksObj) {
        if (clicksObj == null) {
            return null;
        }
        if (clicksObj instanceof Integer) {
            return (Integer) clicksObj;
        }
        if (clicksObj instanceof Long) {
            return ((Long) clicksObj).intValue();
        }
        if (clicksObj instanceof Number) {
            return ((Number) clicksObj).intValue();
        }
        try {
            return Integer.parseInt(clicksObj.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析点击次数: {}", clicksObj);
            return null;
        }
    }
}

