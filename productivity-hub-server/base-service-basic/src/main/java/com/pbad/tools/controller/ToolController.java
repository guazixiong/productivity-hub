package com.pbad.basic.tools.controller;

import com.pbad.tools.domain.dto.ToolTrackDTO;
import com.pbad.tools.domain.vo.ToolStatVO;
import com.pbad.tools.service.ToolService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工具控制器.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /**
     * 获取工具使用统计
     *
     * @return 工具统计列表
     */
    @GetMapping("/stats")
    public ApiResponse<List<ToolStatVO>> getToolStats() {
        List<ToolStatVO> stats = toolService.getToolStats();
        return ApiResponse.ok(stats);
    }

    /**
     * 记录工具使用
     *
     * @param trackDTO 记录请求
     * @return 更新后的完整统计列表
     */
    @PostMapping("/track")
    public ApiResponse<List<ToolStatVO>> trackToolUsage(@RequestBody ToolTrackDTO trackDTO) {
        List<ToolStatVO> stats = toolService.trackToolUsage(trackDTO);
        return ApiResponse.ok(stats);
    }
}

