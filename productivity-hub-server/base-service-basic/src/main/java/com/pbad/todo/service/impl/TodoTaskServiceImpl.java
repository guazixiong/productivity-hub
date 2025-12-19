package com.pbad.todo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.todo.domain.dto.TodoTaskCreateDTO;
import com.pbad.todo.domain.dto.TodoTaskInterruptDTO;
import com.pbad.todo.domain.dto.TodoTaskUpdateDTO;
import com.pbad.todo.domain.enums.TodoEventType;
import com.pbad.todo.domain.enums.TodoPriority;
import com.pbad.todo.domain.enums.TodoStatus;
import com.pbad.todo.domain.po.TodoDailyStatsPO;
import com.pbad.todo.domain.po.TodoEventPO;
import com.pbad.todo.domain.po.TodoModulePO;
import com.pbad.todo.domain.po.TodoModuleStatsPO;
import com.pbad.todo.domain.po.TodoTaskPO;
import com.pbad.todo.domain.vo.TodoEventVO;
import com.pbad.todo.domain.vo.TodoStatsVO;
import com.pbad.todo.domain.vo.TodoTaskVO;
import com.pbad.todo.mapper.TodoEventMapper;
import com.pbad.todo.mapper.TodoModuleMapper;
import com.pbad.todo.mapper.TodoTaskMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pbad.todo.service.TodoTaskService;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoTaskServiceImpl implements TodoTaskService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TodoTaskMapper taskMapper;
    private final TodoModuleMapper moduleMapper;
    private final TodoEventMapper eventMapper;
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(readOnly = true)
    public List<TodoTaskVO> listTasks(String userId, String moduleId, String status) {
        List<TodoTaskPO> tasks = taskMapper.selectByUser(userId, moduleId, status);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TodoTaskVO> listTasksWithPage(String userId, String moduleId, String status, int pageNum, int pageSize) {
        // 使用 PageHelper 进行分页
        PageHelper.startPage(pageNum, pageSize);
        
        // 查询分页数据（PageHelper 会自动拦截并添加 LIMIT 子句）
        List<TodoTaskPO> tasks = taskMapper.selectByUser(userId, moduleId, status);
        
        // 获取分页信息
        PageInfo<TodoTaskPO> pageInfo = new PageInfo<>(tasks);
        
        // 转换为 VO
        List<TodoTaskVO> items = tasks.stream().map(this::convertToVO).collect(Collectors.toList());
        
        // 转换为 PageResult
        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), items);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoTaskVO getTask(String id, String userId) {
        TodoTaskPO task = taskMapper.selectById(id, userId);
        if (task == null) {
            throw new BusinessException("404", "任务不存在");
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO createTask(TodoTaskCreateDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("400", "任务标题不能为空");
        }
        if (!StringUtils.hasText(dto.getModuleId())) {
            throw new BusinessException("400", "所属模块必填");
        }
        TodoModulePO module = requireModule(dto.getModuleId(), userId);

        TodoTaskPO task = new TodoTaskPO();
        task.setId(idGeneratorApi.generateId());
        task.setUserId(userId);
        task.setModuleId(module.getId());
        task.setTitle(dto.getTitle().trim());
        task.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        task.setPriority(normalizePriority(dto.getPriority()).name());
        task.setDueDate(parseDueDate(dto.getDueDate()));
        task.setTagsJson(serializeTags(dto.getTags()));
        task.setStatus(TodoStatus.PENDING.name());
        Date now = new Date();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setLastEventAt(now);
        task.setDurationMs(0L);
        task.setPausedDurationMs(0L);

        int inserted = taskMapper.insertTask(task);
        if (inserted <= 0) {
            throw new BusinessException("500", "创建任务失败");
        }
        recordEvent(task.getId(), userId, TodoEventType.CREATE, now, null);
        return convertToVO(taskMapper.selectById(task.getId(), userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO updateTask(TodoTaskUpdateDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getId())) {
            throw new BusinessException("400", "任务ID不能为空");
        }
        TodoTaskPO task = requireTask(dto.getId(), userId);
        if (TodoStatus.IN_PROGRESS.name().equals(task.getStatus())) {
            throw new BusinessException("400", "进行中的任务请先暂停或完成后再编辑");
        }
        if (StringUtils.hasText(dto.getTitle())) {
            task.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription().trim());
        }
        if (StringUtils.hasText(dto.getModuleId())) {
            TodoModulePO module = requireModule(dto.getModuleId(), userId);
            task.setModuleId(module.getId());
        }
        if (StringUtils.hasText(dto.getPriority())) {
            task.setPriority(normalizePriority(dto.getPriority()).name());
        }
        if (dto.getTags() != null) {
            task.setTagsJson(serializeTags(dto.getTags()));
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(parseDueDate(dto.getDueDate()));
        }
        task.setUpdatedAt(new Date());
        int updated = taskMapper.updateTask(task);
        if (updated <= 0) {
            throw new BusinessException("500", "更新任务失败");
        }
        return convertToVO(taskMapper.selectById(task.getId(), userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(String id, String userId) {
        TodoTaskPO task = requireTask(id, userId);
        if (TodoStatus.IN_PROGRESS.name().equals(task.getStatus())) {
            throw new BusinessException("400", "进行中的任务不能删除，请先暂停或完成");
        }
        int deleted = taskMapper.deleteTask(id, userId);
        if (deleted <= 0) {
            throw new BusinessException("500", "删除任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO startTask(String id, String userId) {
        TodoTaskPO task = requireTask(id, userId);
        TodoStatus previousStatus = parseStatus(task.getStatus());
        if (!previousStatus.canStart()) {
            throw new BusinessException("400", "任务状态不允许开始");
        }
        Date now = new Date();
        pauseOtherRunningTasks(userId, id, now);

        if (task.getPauseStartedAt() != null) {
            long paused = now.getTime() - task.getPauseStartedAt().getTime();
            task.setPausedDurationMs(safeDuration(task.getPausedDurationMs()) + Math.max(paused, 0));
            task.setPauseStartedAt(null);
        }
        if (task.getStartedAt() == null) {
            task.setStartedAt(now);
        }
        task.setActiveStartAt(now);
        task.setEndedAt(null);
        task.setStatus(TodoStatus.IN_PROGRESS.name());
        task.setLastEventAt(now);
        task.setUpdatedAt(now);

        int updated = taskMapper.updateTask(task);
        if (updated <= 0) {
            throw new BusinessException("500", "开始任务失败");
        }
        TodoEventType eventType = TodoStatus.PAUSED.equals(previousStatus) ? TodoEventType.RESUME : TodoEventType.START;
        recordEvent(task.getId(), userId, eventType, now, null);
        return convertToVO(taskMapper.selectById(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO pauseTask(String id, String userId, boolean system) {
        TodoTaskPO task = requireTask(id, userId);
        if (!TodoStatus.IN_PROGRESS.name().equals(task.getStatus())) {
            throw new BusinessException("400", "仅进行中的任务可以暂停");
        }
        Date now = new Date();
        task = pauseInternal(task, now);
        recordEvent(task.getId(), userId, TodoEventType.PAUSE, now, system ? "system-auto" : null);
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO resumeTask(String id, String userId) {
        return startTask(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO completeTask(String id, String userId) {
        TodoTaskPO task = requireTask(id, userId);
        if (TodoStatus.COMPLETED.name().equals(task.getStatus())) {
            return convertToVO(task);
        }
        Date now = new Date();
        accumulateRunningDuration(task, now);
        accumulatePausedDuration(task, now);

        task.setStatus(TodoStatus.COMPLETED.name());
        task.setEndedAt(now);
        task.setActiveStartAt(null);
        task.setPauseStartedAt(null);
        task.setLastEventAt(now);
        task.setUpdatedAt(now);

        int updated = taskMapper.updateTask(task);
        if (updated <= 0) {
            throw new BusinessException("500", "完成任务失败");
        }
        recordEvent(task.getId(), userId, TodoEventType.COMPLETE, now, null);
        return convertToVO(taskMapper.selectById(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoTaskVO interruptTask(String id, String userId, TodoTaskInterruptDTO interruptDTO, boolean system) {
        TodoTaskPO task = requireTask(id, userId);
        if (task.getStatus() != null && TodoStatus.COMPLETED.name().equals(task.getStatus())) {
            throw new BusinessException("400", "已完成的任务无法中断");
        }
        Date now = new Date();
        accumulateRunningDuration(task, now);
        accumulatePausedDuration(task, now);

        task.setStatus(TodoStatus.INTERRUPTED.name());
        task.setEndedAt(now);
        task.setActiveStartAt(null);
        task.setPauseStartedAt(null);
        task.setLastEventAt(now);
        task.setUpdatedAt(now);

        int updated = taskMapper.updateTask(task);
        if (updated <= 0) {
            throw new BusinessException("500", "中断任务失败");
        }
        String payload = interruptDTO != null ? interruptDTO.getReason() : null;
        recordEvent(task.getId(), userId, system ? TodoEventType.SYSTEM_INTERRUPT : TodoEventType.INTERRUPT, now, payload);
        return convertToVO(taskMapper.selectById(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public TodoTaskVO getActiveTask(String userId) {
        List<TodoTaskPO> running = taskMapper.selectByStatuses(userId, Collections.singletonList(TodoStatus.IN_PROGRESS.name()));
        if (CollectionUtils.isEmpty(running)) {
            return null;
        }
        return convertToVO(running.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public TodoStatsVO getStats(String userId, LocalDateTime start, LocalDateTime end) {
        TodoStatsVO vo = new TodoStatsVO();
        Long total = taskMapper.countByStatus(userId, null);
        Long completed = taskMapper.countByStatus(userId, TodoStatus.COMPLETED.name());
        Long inProgress = taskMapper.countByStatus(userId, TodoStatus.IN_PROGRESS.name());
        Long interrupted = taskMapper.countByStatus(userId, TodoStatus.INTERRUPTED.name());
        Long duration = taskMapper.sumDuration(userId, start, end);

        vo.setTotalTasks(safeCount(total));
        vo.setCompletedTasks(safeCount(completed));
        vo.setInProgressTasks(safeCount(inProgress));
        vo.setInterruptedTasks(safeCount(interrupted));
        vo.setTotalDurationMs(duration == null ? 0L : duration);

        List<TodoModuleStatsPO> moduleStats = taskMapper.aggregateByModule(userId, start, end);
        if (moduleStats != null) {
            vo.setModuleStats(moduleStats.stream().map(this::convertModuleStat).collect(Collectors.toList()));
        }

        List<TodoDailyStatsPO> dailyStats = taskMapper.aggregateDaily(userId, start, end);
        if (dailyStats != null) {
            vo.setTimeline(dailyStats.stream().map(this::convertDailyStat).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoEventVO> listEvents(String todoId, String userId) {
        List<TodoEventPO> events = eventMapper.selectByTodoId(todoId, userId);
        return events.stream().map(this::convertEventToVO).collect(Collectors.toList());
    }

    private TodoEventVO convertEventToVO(TodoEventPO po) {
        TodoEventVO vo = new TodoEventVO();
        vo.setId(po.getId());
        vo.setTodoId(po.getTodoId());
        vo.setEventType(po.getEventType());
        vo.setOccurredAt(po.getOccurredAt());
        vo.setPayload(po.getPayload());
        return vo;
    }

    private TodoStatsVO.ModuleStat convertModuleStat(TodoModuleStatsPO po) {
        TodoStatsVO.ModuleStat stat = new TodoStatsVO.ModuleStat();
        stat.setModuleId(po.getModuleId());
        stat.setModuleName(po.getModuleName());
        stat.setTotalTasks(safeCount(po.getTotalTasks()));
        stat.setCompletedTasks(safeCount(po.getCompletedTasks()));
        stat.setDurationMs(po.getDurationMs() == null ? 0L : po.getDurationMs());
        return stat;
    }

    private TodoStatsVO.DailyStat convertDailyStat(TodoDailyStatsPO po) {
        TodoStatsVO.DailyStat stat = new TodoStatsVO.DailyStat();
        if (po.getDate() != null) {
            stat.setDate(java.time.LocalDate.parse(po.getDate()));
        }
        stat.setCompletedTasks(safeCount(po.getCompletedTasks()));
        stat.setDurationMs(po.getDurationMs() == null ? 0L : po.getDurationMs());
        return stat;
    }

    private TodoTaskPO pauseInternal(TodoTaskPO task, Date now) {
        accumulateRunningDuration(task, now);
        task.setPauseStartedAt(now);
        task.setActiveStartAt(null);
        task.setStatus(TodoStatus.PAUSED.name());
        task.setLastEventAt(now);
        task.setUpdatedAt(now);
        taskMapper.updateTask(task);
        return taskMapper.selectById(task.getId(), task.getUserId());
    }

    private void pauseOtherRunningTasks(String userId, String excludeTaskId, Date now) {
        List<TodoTaskPO> running = taskMapper.selectByStatuses(userId, Collections.singletonList(TodoStatus.IN_PROGRESS.name()));
        if (CollectionUtils.isEmpty(running)) {
            return;
        }
        for (TodoTaskPO active : running) {
            if (Objects.equals(active.getId(), excludeTaskId)) {
                continue;
            }
            TodoTaskPO paused = pauseInternal(active, now);
            recordEvent(paused.getId(), userId, TodoEventType.PAUSE, now, "auto-switch");
        }
    }

    private void accumulateRunningDuration(TodoTaskPO task, Date now) {
        if (task.getActiveStartAt() == null) {
            return;
        }
        long elapsed = now.getTime() - task.getActiveStartAt().getTime();
        task.setDurationMs(safeDuration(task.getDurationMs()) + Math.max(elapsed, 0));
        task.setActiveStartAt(null);
    }

    private void accumulatePausedDuration(TodoTaskPO task, Date now) {
        if (task.getPauseStartedAt() == null) {
            return;
        }
        long paused = now.getTime() - task.getPauseStartedAt().getTime();
        task.setPausedDurationMs(safeDuration(task.getPausedDurationMs()) + Math.max(paused, 0));
        task.setPauseStartedAt(null);
    }

    private TodoTaskPO requireTask(String id, String userId) {
        TodoTaskPO task = taskMapper.selectById(id, userId);
        if (task == null) {
            throw new BusinessException("404", "任务不存在");
        }
        return task;
    }

    private TodoModulePO requireModule(String moduleId, String userId) {
        TodoModulePO module = moduleMapper.selectById(moduleId, userId);
        if (module == null) {
            throw new BusinessException("404", "模块不存在或无权限");
        }
        return module;
    }

    private TodoPriority normalizePriority(String priority) {
        if (!StringUtils.hasText(priority)) {
            return TodoPriority.defaultPriority();
        }
        try {
            return TodoPriority.valueOf(priority.trim());
        } catch (IllegalArgumentException ex) {
            return TodoPriority.defaultPriority();
        }
    }

    private long safeDuration(Long value) {
        return value == null ? 0L : value;
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private TodoTaskVO convertToVO(TodoTaskPO po) {
        TodoTaskVO vo = new TodoTaskVO();
        vo.setId(po.getId());
        vo.setModuleId(po.getModuleId());
        TodoModulePO module = moduleMapper.selectById(po.getModuleId(), po.getUserId());
        vo.setModuleName(module != null ? module.getName() : null);
        vo.setTitle(po.getTitle());
        vo.setDescription(po.getDescription());
        vo.setPriority(po.getPriority());
        vo.setTags(deserializeTags(po.getTagsJson()));
        vo.setStatus(po.getStatus());
        vo.setDueDate(po.getDueDate());
        vo.setStartedAt(po.getStartedAt());
        vo.setEndedAt(po.getEndedAt());
        vo.setActiveStartAt(po.getActiveStartAt());
        vo.setPauseStartedAt(po.getPauseStartedAt());
        // 对于正在运行的任务，需要加上当前运行时间
        long durationMs = safeDuration(po.getDurationMs());
        if (TodoStatus.IN_PROGRESS.name().equals(po.getStatus()) && po.getActiveStartAt() != null) {
            long currentRunning = System.currentTimeMillis() - po.getActiveStartAt().getTime();
            durationMs += Math.max(currentRunning, 0);
        }
        vo.setDurationMs(durationMs);
        vo.setPausedDurationMs(safeDuration(po.getPausedDurationMs()));
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }

    /**
     * 解析截止日期字符串（yyyy-MM-dd）为 Date，null 或空字符串返回 null。
     */
    private Date parseDueDate(String dueDateStr) {
        if (!StringUtils.hasText(dueDateStr)) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dueDateStr.trim());
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ex) {
            // 不抛异常，避免因为一个字段格式问题影响所有操作
            log.warn("解析截止日期失败: {}", dueDateStr, ex);
            return null;
        }
    }

    private String serializeTags(List<String> tags) {
        try {
            if (CollectionUtils.isEmpty(tags)) {
                return OBJECT_MAPPER.writeValueAsString(Collections.emptyList());
            }
            List<String> filtered = tags.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toList());
            return OBJECT_MAPPER.writeValueAsString(filtered);
        } catch (Exception ex) {
            log.warn("序列化标签失败", ex);
            throw new BusinessException("400", "标签格式不正确");
        }
    }

    private List<String> deserializeTags(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(tagsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            log.warn("反序列化标签失败: {}", tagsJson, ex);
            return Collections.emptyList();
        }
    }

    private void recordEvent(String todoId, String userId, TodoEventType type, Date when, String payload) {
        TodoEventPO event = new TodoEventPO();
        event.setId(idGeneratorApi.generateId());
        event.setTodoId(todoId);
        event.setUserId(userId);
        event.setEventType(type.name());
        event.setOccurredAt(when);
        event.setPayload(payload);
        event.setCreatedAt(when);
        eventMapper.insertEvent(event);
    }

    private TodoStatus parseStatus(String status) {
        try {
            return TodoStatus.valueOf(status);
        } catch (Exception e) {
            return TodoStatus.PENDING;
        }
    }
}

