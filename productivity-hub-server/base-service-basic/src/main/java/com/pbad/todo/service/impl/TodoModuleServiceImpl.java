package com.pbad.todo.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.todo.domain.dto.TodoModuleCreateDTO;
import com.pbad.todo.domain.dto.TodoModuleUpdateDTO;
import com.pbad.todo.domain.po.TodoModulePO;
import com.pbad.todo.domain.po.TodoModuleStatsPO;
import com.pbad.todo.domain.vo.TodoModuleVO;
import com.pbad.todo.mapper.TodoModuleMapper;
import com.pbad.todo.mapper.TodoTaskMapper;
import com.pbad.todo.service.TodoModuleService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoModuleServiceImpl implements TodoModuleService {

    private final TodoModuleMapper moduleMapper;
    private final TodoTaskMapper taskMapper;
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(readOnly = true)
    public List<TodoModuleVO> listModules(String userId) {
        List<TodoModulePO> modules = moduleMapper.selectByUser(userId);
        Map<String, TodoModuleStatsPO> statsMap = buildStatsMap(userId);
        return modules.stream()
                .map(po -> convertToVO(po, statsMap.get(po.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoModuleVO createModule(TodoModuleCreateDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException("400", "模块名称不能为空");
        }
        ensureModuleNameUnique(userId, dto.getName(), null);

        TodoModulePO po = new TodoModulePO();
        po.setId(idGeneratorApi.generateId());
        po.setUserId(userId);
        po.setName(dto.getName().trim());
        po.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        po.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "ENABLED");
        po.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        Date now = new Date();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        int inserted = moduleMapper.insert(po);
        if (inserted <= 0) {
            throw new BusinessException("500", "创建模块失败");
        }
        return convertToVO(po, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoModuleVO updateModule(TodoModuleUpdateDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getId())) {
            throw new BusinessException("400", "模块ID不能为空");
        }
        TodoModulePO existing = moduleMapper.selectById(dto.getId(), userId);
        if (existing == null) {
            throw new BusinessException("404", "模块不存在或无权访问");
        }
        if (StringUtils.hasText(dto.getName())) {
            ensureModuleNameUnique(userId, dto.getName(), dto.getId());
            existing.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim());
        }
        if (dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            existing.setStatus(dto.getStatus());
        }
        existing.setUpdatedAt(new Date());

        int updated = moduleMapper.update(existing);
        if (updated <= 0) {
            throw new BusinessException("500", "更新模块失败");
        }
        Map<String, TodoModuleStatsPO> statsMap = buildStatsMap(userId);
        return convertToVO(existing, statsMap.get(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "模块ID不能为空");
        }
        TodoModulePO existing = moduleMapper.selectById(id, userId);
        if (existing == null) {
            throw new BusinessException("404", "模块不存在或无权访问");
        }
        int taskCount = moduleMapper.countTasksByModule(id, userId);
        if (taskCount > 0) {
            throw new BusinessException("400", "模块下存在任务，无法删除");
        }
        int deleted = moduleMapper.delete(id, userId);
        if (deleted <= 0) {
            throw new BusinessException("500", "删除模块失败");
        }
    }

    private void ensureModuleNameUnique(String userId, String name, String excludeId) {
        List<TodoModulePO> modules = moduleMapper.selectByUser(userId);
        boolean duplicated = modules.stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(name.trim())
                        && (excludeId == null || !excludeId.equals(m.getId())));
        if (duplicated) {
            throw new BusinessException("400", "同名模块已存在");
        }
    }

    private Map<String, TodoModuleStatsPO> buildStatsMap(String userId) {
        List<TodoModuleStatsPO> stats = taskMapper.aggregateByModule(userId, null, null);
        Map<String, TodoModuleStatsPO> map = new HashMap<>();
        if (stats != null) {
            for (TodoModuleStatsPO stat : stats) {
                map.put(stat.getModuleId(), stat);
            }
        }
        return map;
    }

    private TodoModuleVO convertToVO(TodoModulePO po, TodoModuleStatsPO stat) {
        TodoModuleVO vo = new TodoModuleVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setDescription(po.getDescription());
        vo.setStatus(po.getStatus());
        vo.setSortOrder(po.getSortOrder());
        if (stat != null) {
            vo.setTotalTasks(stat.getTotalTasks());
            vo.setCompletedTasks(stat.getCompletedTasks());
            vo.setTotalDurationMs(stat.getDurationMs());
        } else {
            vo.setTotalTasks(0L);
            vo.setCompletedTasks(0L);
            vo.setTotalDurationMs(0L);
        }
        return vo;
    }
}

