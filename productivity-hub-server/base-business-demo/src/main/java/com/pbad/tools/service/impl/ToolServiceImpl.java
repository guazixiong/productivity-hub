package com.pbad.tools.service.impl;

import com.pbad.tools.domain.dto.ToolTrackDTO;
import com.pbad.tools.domain.po.ToolStatPO;
import com.pbad.tools.domain.vo.ToolStatVO;
import com.pbad.tools.mapper.ToolStatMapper;
import com.pbad.tools.service.ToolService;
import common.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final ToolStatMapper toolStatMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ToolStatVO> getToolStats() {
        List<ToolStatPO> poList = toolStatMapper.selectAllOrderByClicks();
        return poList.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ToolStatVO> trackToolUsage(ToolTrackDTO trackDTO) {
        if (trackDTO == null || trackDTO.getToolId() == null || trackDTO.getToolId().isEmpty()) {
            throw new IllegalArgumentException("工具ID不能为空");
        }

        String toolId = trackDTO.getToolId();

        // 查询工具统计
        ToolStatPO toolStat = toolStatMapper.selectById(toolId);

        if (toolStat == null) {
            // 如果工具不存在，自动创建新记录
            toolStat = new ToolStatPO();
            toolStat.setId(toolId);
            toolStat.setToolName(toolId); // 使用 toolId 作为默认名称
            toolStat.setClicks(1);
            toolStatMapper.insert(toolStat);
        } else {
            // 更新点击次数
            toolStatMapper.incrementClicks(toolId);
        }

        // 返回更新后的完整统计列表
        return getToolStats();
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

