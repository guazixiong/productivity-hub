package com.pbad.tools.service.impl;

import com.pbad.tools.domain.dto.ToolTrackDTO;
import com.pbad.tools.domain.po.ToolStatPO;
import com.pbad.tools.domain.vo.ToolStatVO;
import com.pbad.tools.mapper.ToolStatMapper;
import com.pbad.tools.service.ToolService;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 工具服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private static final String REDIS_KEY_PREFIX = "tool:stat:";
    private static final String REDIS_STATS_CACHE_KEY = "tool:stats:list";
    private static final long REDIS_CACHE_EXPIRE_HOURS = 24; // Redis缓存过期时间24小时

    private final ToolStatMapper toolStatMapper;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(readOnly = true)
    public List<ToolStatVO> getToolStats() {
        // 先从Redis缓存获取
        Object cachedStats = redisUtil.getValue(REDIS_STATS_CACHE_KEY);
        if (cachedStats != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cachedList = (List<Map<String, Object>>) cachedStats;
                return cachedList.stream()
                        .map(this::mapToVO)
                        .sorted((a, b) -> b.getClicks().compareTo(a.getClicks()))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("从Redis缓存读取工具统计失败，将从数据库获取", e);
            }
        }

        // 从数据库获取
        List<ToolStatPO> poList = toolStatMapper.selectAllOrderByClicks();
        List<ToolStatVO> voList = poList.stream().map(this::convertToVO).collect(Collectors.toList());

        // 合并Redis中的实时数据
        voList = mergeWithRedisData(voList);

        // 更新Redis缓存
        updateStatsCache(voList);

        return voList;
    }

    @Override
    public List<ToolStatVO> trackToolUsage(ToolTrackDTO trackDTO) {
        if (trackDTO == null || !StringUtils.hasText(trackDTO.getToolId())) {
            throw new IllegalArgumentException("工具ID不能为空");
        }

        String toolId = trackDTO.getToolId();
        String redisKey = REDIS_KEY_PREFIX + toolId;

        // 使用Redis原子操作自动+1，并设置过期时间
        Long clicks = redisUtil.increment(redisKey, REDIS_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        // 清除统计列表缓存，下次查询时重新构建
        redisUtil.delete(REDIS_STATS_CACHE_KEY);

        log.debug("工具 {} 点击次数已更新为: {}", toolId, clicks);

        // 返回更新后的完整统计列表
        return getToolStats();
    }

    /**
     * 合并Redis中的实时数据
     */
    private List<ToolStatVO> mergeWithRedisData(List<ToolStatVO> dbStats) {
        Map<String, ToolStatVO> statMap = dbStats.stream()
                .collect(Collectors.toMap(ToolStatVO::getId, vo -> vo, (a, b) -> a));

        // 获取所有Redis中的工具统计key
        Set<String> redisKeys = redisUtil.keys(REDIS_KEY_PREFIX + "*");
        if (redisKeys != null && !redisKeys.isEmpty()) {
            for (String redisKey : redisKeys) {
                String toolId = redisKey.substring(REDIS_KEY_PREFIX.length());
                Object clicksObj = redisUtil.getValue(redisKey);
                if (clicksObj != null) {
                    Integer clicks = parseClicks(clicksObj);
                    if (clicks != null && clicks > 0) {
                        ToolStatVO vo = statMap.get(toolId);
                        if (vo != null) {
                            // 合并：Redis中的增量 + 数据库中的基础值
                            vo.setClicks(vo.getClicks() + clicks);
                        } else {
                            // Redis中有但数据库中没有，创建新记录
                            vo = new ToolStatVO();
                            vo.setId(toolId);
                            vo.setName(toolId); // 默认使用toolId作为名称
                            vo.setClicks(clicks);
                            statMap.put(toolId, vo);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(statMap.values());
    }

    /**
     * 更新统计列表缓存
     */
    private void updateStatsCache(List<ToolStatVO> voList) {
        try {
            List<Map<String, Object>> cacheData = voList.stream()
                    .map(vo -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", vo.getId());
                        map.put("name", vo.getName());
                        map.put("clicks", vo.getClicks());
                        return map;
                    })
                    .collect(Collectors.toList());
            redisUtil.setKey(REDIS_STATS_CACHE_KEY, cacheData, REDIS_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("更新Redis统计缓存失败", e);
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

    /**
     * Map转换为VO
     */
    private ToolStatVO mapToVO(Map<String, Object> map) {
        ToolStatVO vo = new ToolStatVO();
        vo.setId((String) map.get("id"));
        vo.setName((String) map.get("name"));
        Object clicksObj = map.get("clicks");
        vo.setClicks(parseClicks(clicksObj));
        return vo;
    }

    /**
     * 转换为 VO
     */
    private ToolStatVO convertToVO(ToolStatPO po) {
        ToolStatVO vo = new ToolStatVO();
        vo.setId(po.getId());
        vo.setName(po.getToolName());
        vo.setClicks(po.getClicks());
        return vo;
    }
}

