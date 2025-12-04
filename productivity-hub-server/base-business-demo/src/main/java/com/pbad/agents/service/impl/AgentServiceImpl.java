package com.pbad.agents.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.agents.domain.dto.AgentInvokeDTO;
import com.pbad.agents.domain.po.AgentLogPO;
import com.pbad.agents.domain.po.AgentPO;
import com.pbad.agents.domain.vo.AgentInvokeResponseVO;
import com.pbad.agents.domain.vo.AgentLogVO;
import com.pbad.agents.domain.vo.AgentSummaryVO;
import com.pbad.agents.mapper.AgentLogMapper;
import com.pbad.agents.mapper.AgentMapper;
import com.pbad.agents.service.AgentService;
import com.pbad.generator.api.IdGeneratorApi;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能体服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentMapper agentMapper;
    private final AgentLogMapper agentLogMapper;
    private final IdGeneratorApi idGeneratorApi;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    @Override
    @Transactional(readOnly = true)
    public List<AgentSummaryVO> getAgentList() {
        List<AgentPO> poList = agentMapper.selectAll();
        return poList.stream().map(this::convertToSummaryVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentInvokeResponseVO invokeAgent(AgentInvokeDTO invokeDTO) {
        // 参数校验
        if (invokeDTO == null || invokeDTO.getAgentId() == null || invokeDTO.getAgentId().isEmpty()) {
            throw new BusinessException("400", "智能体ID不能为空");
        }

        String agentId = invokeDTO.getAgentId();

        // 查询智能体
        AgentPO agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new BusinessException("404", "智能体不存在");
        }

        // 生成任务ID
        String taskId = "task-" + idGeneratorApi.generateId();

        // 记录开始时间
        String startedAt = ISO_FORMATTER.format(Instant.now());

        // 创建日志记录
        AgentLogPO agentLogPO = new AgentLogPO();
        agentLogPO.setId(taskId);
        agentLogPO.setAgentId(agentId);
        agentLogPO.setAgentName(agent.getName());
        agentLogPO.setStatus("running");
        agentLogPO.setDuration(0);
        agentLogPO.setInputData(JSON.toJSONString(invokeDTO.getInput()));

        // 判断调用模式
        String mode = invokeDTO.getMode() != null ? invokeDTO.getMode() : "sync";

        if ("sync".equals(mode)) {
            // 同步模式：立即执行
            try {
                String output = executeAgent(agent, invokeDTO.getInput());
                String finishedAt = ISO_FORMATTER.format(Instant.now());

                // 计算耗时（模拟，使用智能体的平均延迟）
                int duration = agent.getLatencyMs() != null ? agent.getLatencyMs() : 1000;

                // 更新日志
                agentLogPO.setStatus("success");
                agentLogPO.setDuration(duration);
                agentLogPO.setOutputData(output);
                agentLogMapper.insert(agentLogPO);

                // 构建响应
                AgentInvokeResponseVO response = new AgentInvokeResponseVO();
                response.setTaskId(taskId);
                response.setStatus("success");
                response.setOutput(output);
                response.setStartedAt(startedAt);
                response.setFinishedAt(finishedAt);
                return response;
            } catch (Exception e) {
                log.error("执行智能体失败: {}", e.getMessage(), e);
                agentLogPO.setStatus("failed");
                agentLogPO.setOutputData("执行失败: " + e.getMessage());
                agentLogMapper.insert(agentLogPO);

                throw new BusinessException("500", "执行智能体失败: " + e.getMessage());
            }
        } else {
            // 异步模式：先返回，后台执行
            agentLogPO.setOutputData("");
            agentLogMapper.insert(agentLogPO);

            // 异步执行
            executeAgentAsync(taskId, agent, invokeDTO.getInput());

            // 构建响应
            AgentInvokeResponseVO response = new AgentInvokeResponseVO();
            response.setTaskId(taskId);
            response.setStatus("running");
            response.setOutput("");
            response.setStartedAt(startedAt);
            return response;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AgentLogVO> getAgentLogs(int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        List<AgentLogPO> poList = agentLogMapper.selectPage(offset, safePageSize);
        long total = agentLogMapper.countAll();
        List<AgentLogVO> items = poList.stream().map(this::convertToLogVO).collect(Collectors.toList());
        return PageResult.of(safePageNum, safePageSize, total, items);
    }

    /**
     * 执行智能体（模拟）
     */
    private String executeAgent(AgentPO agent, Map<String, Object> input) {
        // TODO: 实际生产环境需要调用真实的智能体服务
        // 这里仅做模拟实现
        String prompt = input != null && input.containsKey("prompt") 
                ? (String) input.get("prompt") 
                : "默认提示";

        // 模拟处理时间
        try {
            Thread.sleep(agent.getLatencyMs() != null ? agent.getLatencyMs() : 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 返回模拟结果
        return "智能体响应：处理完成 - " + prompt;
    }

    /**
     * 异步执行智能体
     */
    @Async
    public void executeAgentAsync(String taskId, AgentPO agent, Map<String, Object> input) {
        try {
            String output = executeAgent(agent, input);
            int duration = agent.getLatencyMs() != null ? agent.getLatencyMs() : 1000;

            // 更新日志
            AgentLogPO log = new AgentLogPO();
            log.setId(taskId);
            log.setStatus("success");
            log.setDuration(duration);
            log.setOutputData(output);
            agentLogMapper.update(log);
        } catch (Exception e) {
            log.error("异步执行智能体失败: {}", e.getMessage(), e);
            AgentLogPO log = new AgentLogPO();
            log.setId(taskId);
            log.setStatus("failed");
            log.setOutputData("执行失败: " + e.getMessage());
            agentLogMapper.update(log);
        }
    }

    /**
     * 转换为摘要 VO
     */
    private AgentSummaryVO convertToSummaryVO(AgentPO po) {
        AgentSummaryVO vo = new AgentSummaryVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setDescription(po.getDescription());
        vo.setVersion(po.getVersion());
        vo.setLatencyMs(po.getLatencyMs());
        vo.setOwner(po.getOwner());

        // 解析标签 JSON 字符串
        if (po.getTags() != null && !po.getTags().isEmpty()) {
            try {
                List<String> tags = JSON.parseArray(po.getTags(), String.class);
                vo.setTags(tags);
            } catch (Exception e) {
                log.warn("解析智能体标签失败: {}", e.getMessage());
                vo.setTags(null);
            }
        }

        return vo;
    }

    /**
     * 转换为日志 VO
     */
    private AgentLogVO convertToLogVO(AgentLogPO po) {
        AgentLogVO vo = new AgentLogVO();
        vo.setId(po.getId());
        vo.setAgentId(po.getAgentId());
        vo.setAgentName(po.getAgentName());
        vo.setStatus(po.getStatus());
        vo.setDuration(po.getDuration());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setOutput(po.getOutputData());

        // 解析输入数据，转换为字符串（可能被截断）
        if (po.getInputData() != null && !po.getInputData().isEmpty()) {
            try {
                Map<String, Object> input = JSON.parseObject(po.getInputData(), Map.class);
                // 提取 prompt 字段作为输入内容
                if (input.containsKey("prompt")) {
                    vo.setInput((String) input.get("prompt"));
                } else {
                    vo.setInput(po.getInputData().length() > 100 
                            ? po.getInputData().substring(0, 100) + "..." 
                            : po.getInputData());
                }
            } catch (Exception e) {
                vo.setInput(po.getInputData().length() > 100 
                        ? po.getInputData().substring(0, 100) + "..." 
                        : po.getInputData());
            }
        }

        return vo;
    }
}

