package com.pbad.agents.controller;

import com.pbad.agents.domain.dto.AgentInvokeDTO;
import com.pbad.agents.domain.vo.AgentInvokeResponseVO;
import com.pbad.agents.domain.vo.AgentLogVO;
import com.pbad.agents.domain.vo.AgentSummaryVO;
import com.pbad.agents.service.AgentService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 智能体控制器.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * 获取智能体列表
     *
     * @return 智能体列表
     */
    @GetMapping
    public ApiResponse<List<AgentSummaryVO>> getAgentList() {
        List<AgentSummaryVO> agents = agentService.getAgentList();
        return ApiResponse.ok(agents);
    }

    /**
     * 调用智能体
     *
     * @param invokeDTO 调用请求
     * @return 调用响应
     */
    @PostMapping("/invoke")
    public ApiResponse<AgentInvokeResponseVO> invokeAgent(@RequestBody AgentInvokeDTO invokeDTO,
                                                          HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId == null) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        AgentInvokeResponseVO response = agentService.invokeAgent(invokeDTO, userId);
        return ApiResponse.ok(response);
    }

    /**
     * 获取调用历史（分页，按时间倒序）
     *
     * @param page     页码（默认 1）
     * @param pageSize 每页条数（默认 10）
     * @return 分页结果
     */
    @GetMapping("/logs")
    public ApiResponse<PageResult<AgentLogVO>> getAgentLogs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId == null) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        PageResult<AgentLogVO> logs = agentService.getAgentLogs(page, pageSize, userId);
        return ApiResponse.ok(logs);
    }

    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        String userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        return userId;
    }
}

