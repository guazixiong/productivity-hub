package com.pbad.tools.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbad.tools.domain.dto.ToolTrackDTO;
import com.pbad.tools.domain.po.ToolStatPO;
import com.pbad.tools.domain.vo.ToolStatVO;
import com.pbad.tools.mapper.ToolStatMapper;
import com.pbad.tools.service.ToolService;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 工具服务实现类.
 *
 * @author: pbad
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<ToolStatVO> getToolStats() {
        // 先从Redis缓存获取
        Object cachedStats = null;
        try {
            cachedStats = redisUtil.getValue(REDIS_STATS_CACHE_KEY);
        } catch (Exception e) {
            // 检查是否是序列化异常（可能被包装在RuntimeException中）
            Throwable cause = e.getCause();
            if (e instanceof SerializationException || 
                (cause != null && cause instanceof SerializationException)) {
                // 处理序列化异常：尝试获取原始字符串并手动解析
                log.warn("Redis反序列化失败，尝试手动解析JSON: {}", e.getMessage());
                try {
                    String rawJson = redisUtil.getRawStringValue(REDIS_STATS_CACHE_KEY);
                    if (rawJson != null && !rawJson.isEmpty()) {
                        // 尝试解析JSON（可能是普通JSON或带类型信息的JSON）
                        List<Map<String, Object>> cachedList = parseRawJson(rawJson);
                        if (cachedList != null) {
                            // 解析成功，删除旧缓存并重新存储（使用正确的序列化方式）
                            redisUtil.delete(REDIS_STATS_CACHE_KEY);
                            List<ToolStatVO> result = cachedList.stream()
                                    .map(this::mapToVO)
                                    .sorted((a, b) -> b.getClicks().compareTo(a.getClicks()))
                                    .collect(Collectors.toList());
                            // 重新缓存数据
                            updateStatsCache(result);
                            return result;
                        }
                    }
                } catch (Exception parseException) {
                    log.warn("手动解析Redis JSON失败，删除缓存并从数据库获取", parseException);
                    // 删除有问题的缓存
                    redisUtil.delete(REDIS_STATS_CACHE_KEY);
                }
            } else {
                log.warn("从Redis缓存读取工具统计失败，将从数据库获取", e);
            }
        }

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
     * 解析原始JSON字符串
     * 支持两种格式：
     * 1. 普通JSON数组: [{"name":"workday",...}]
     * 2. 带类型信息的JSON: ["java.util.ArrayList",[{"name":"workday",...}]]
     *
     * @param rawJson 原始JSON字符串
     * @return 解析后的列表，如果解析失败返回null
     */
    private List<Map<String, Object>> parseRawJson(String rawJson) {
        try {
            // 先尝试作为普通JSON数组解析
            return objectMapper.readValue(rawJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // 如果失败，尝试解析带类型信息的格式
            try {
                List<Object> typeInfoArray = objectMapper.readValue(rawJson, new TypeReference<List<Object>>() {});
                if (typeInfoArray != null && typeInfoArray.size() == 2 && typeInfoArray.get(1) instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) typeInfoArray.get(1);
                    return dataList;
                }
            } catch (Exception e2) {
                log.warn("解析带类型信息的JSON也失败", e2);
            }
            log.warn("无法解析原始JSON: {}", rawJson.substring(0, Math.min(200, rawJson.length())));
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

