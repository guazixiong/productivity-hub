package com.pbad.todo.controller;

import com.pbad.todo.domain.dto.TodoModuleCreateDTO;
import com.pbad.todo.domain.dto.TodoModuleUpdateDTO;
import com.pbad.todo.domain.dto.TodoTaskCreateDTO;
import com.pbad.todo.domain.dto.TodoTaskInterruptDTO;
import com.pbad.todo.domain.dto.TodoTaskUpdateDTO;
import com.pbad.todo.domain.vo.TodoEventVO;
import com.pbad.todo.domain.vo.TodoModuleVO;
import com.pbad.todo.domain.vo.TodoStatsVO;
import com.pbad.todo.domain.vo.TodoTaskVO;
import com.pbad.todo.service.TodoModuleService;
import com.pbad.todo.service.TodoTaskService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final TodoModuleService moduleService;
    private final TodoTaskService taskService;

    // ---------- 模块管理 ----------
    @GetMapping("/modules")
    public ApiResponse<List<TodoModuleVO>> listModules(HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(moduleService.listModules(userId));
    }

    @PostMapping("/modules")
    public ApiResponse<TodoModuleVO> createModule(@RequestBody TodoModuleCreateDTO dto, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(moduleService.createModule(dto, userId));
    }

    @PutMapping("/modules/{id}")
    public ApiResponse<TodoModuleVO> updateModule(@PathVariable String id,
                                                  @RequestBody TodoModuleUpdateDTO dto,
                                                  HttpServletRequest request) {
        String userId = currentUserId(request);
        dto.setId(id);
        return ApiResponse.ok(moduleService.updateModule(dto, userId));
    }

    @DeleteMapping("/modules/{id}")
    public ApiResponse<Void> deleteModule(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        moduleService.deleteModule(id, userId);
        return ApiResponse.ok(null);
    }

    // ---------- 任务管理 ----------
    @GetMapping("/tasks")
    public ApiResponse<?> listTasks(@RequestParam(required = false) String moduleId,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Integer pageNum,
                                     @RequestParam(required = false) Integer pageSize,
                                     HttpServletRequest request) {
        String userId = currentUserId(request);
        // 如果提供了分页参数，返回分页结果
        if (pageNum != null && pageNum > 0 && pageSize != null && pageSize > 0) {
            PageResult<TodoTaskVO> pageResult = taskService.listTasksWithPage(userId, moduleId, status, pageNum, pageSize);
            return ApiResponse.ok(pageResult);
        }
        // 否则返回所有数据（保持向后兼容）
        return ApiResponse.ok(taskService.listTasks(userId, moduleId, status));
    }

    @GetMapping("/tasks/active")
    public ApiResponse<TodoTaskVO> activeTask(HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.getActiveTask(userId));
    }

    @GetMapping("/tasks/{id}")
    public ApiResponse<TodoTaskVO> getTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.getTask(id, userId));
    }

    @PostMapping("/tasks")
    public ApiResponse<TodoTaskVO> createTask(@RequestBody TodoTaskCreateDTO dto, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.createTask(dto, userId));
    }

    @PutMapping("/tasks/{id}")
    public ApiResponse<TodoTaskVO> updateTask(@PathVariable String id,
                                              @RequestBody TodoTaskUpdateDTO dto,
                                              HttpServletRequest request) {
        String userId = currentUserId(request);
        dto.setId(id);
        return ApiResponse.ok(taskService.updateTask(dto, userId));
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        taskService.deleteTask(id, userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/tasks/{id}/start")
    public ApiResponse<TodoTaskVO> startTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.startTask(id, userId));
    }

    @PostMapping("/tasks/{id}/pause")
    public ApiResponse<TodoTaskVO> pauseTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.pauseTask(id, userId, false));
    }

    @PostMapping("/tasks/{id}/resume")
    public ApiResponse<TodoTaskVO> resumeTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.resumeTask(id, userId));
    }

    @PostMapping("/tasks/{id}/complete")
    public ApiResponse<TodoTaskVO> completeTask(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.completeTask(id, userId));
    }

    @PostMapping("/tasks/{id}/interrupt")
    public ApiResponse<TodoTaskVO> interruptTask(@PathVariable String id,
                                                 @RequestBody(required = false) TodoTaskInterruptDTO dto,
                                                 HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.interruptTask(id, userId, dto, false));
    }

    @GetMapping("/tasks/{id}/events")
    public ApiResponse<List<TodoEventVO>> events(@PathVariable String id, HttpServletRequest request) {
        String userId = currentUserId(request);
        return ApiResponse.ok(taskService.listEvents(id, userId));
    }

    // ---------- 统计 ----------
    @GetMapping("/stats/overview")
    public ApiResponse<TodoStatsVO> stats(@RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to,
                                          HttpServletRequest request) {
        String userId = currentUserId(request);
        LocalDateTime start = parseDate(from, false);
        LocalDateTime end = parseDate(to, true);
        return ApiResponse.ok(taskService.getStats(userId, start, end));
    }

    private String currentUserId(HttpServletRequest request) {
        String userId = RequestUserContext.getUserId();
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        throw new BusinessException("401", "未登录或登录已过期");
    }

    private LocalDateTime parseDate(String date, boolean endOfDay) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return endOfDay ? localDate.atTime(23, 59, 59) : localDate.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException("400", "日期格式应为yyyy-MM-dd");
        }
    }
}

