package com.pbad.todo.service;

import com.pbad.todo.domain.dto.TodoTaskCreateDTO;
import com.pbad.todo.domain.dto.TodoTaskInterruptDTO;
import com.pbad.todo.domain.dto.TodoTaskUpdateDTO;
import com.pbad.todo.domain.dto.TodoImportItemDTO;
import com.pbad.todo.domain.vo.TodoEventVO;
import com.pbad.todo.domain.vo.TodoStatsVO;
import com.pbad.todo.domain.vo.TodoTaskVO;
import com.pbad.todo.domain.vo.TodoImportResultVO;
import common.core.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoTaskService {
    List<TodoTaskVO> listTasks(String userId, String moduleId, String status);

    PageResult<TodoTaskVO> listTasksWithPage(String userId, String moduleId, String status, int pageNum, int pageSize);

    TodoTaskVO getTask(String id, String userId);

    TodoTaskVO createTask(TodoTaskCreateDTO dto, String userId);

    TodoTaskVO updateTask(TodoTaskUpdateDTO dto, String userId);

    void deleteTask(String id, String userId);

    void batchDeleteTasks(List<String> ids, String userId);

    TodoTaskVO startTask(String id, String userId);

    TodoTaskVO pauseTask(String id, String userId, boolean system);

    TodoTaskVO resumeTask(String id, String userId);

    TodoTaskVO completeTask(String id, String userId);

    TodoTaskVO interruptTask(String id, String userId, TodoTaskInterruptDTO interruptDTO, boolean system);

    TodoTaskVO getActiveTask(String userId);

    TodoStatsVO getStats(String userId, LocalDateTime start, LocalDateTime end);

    List<TodoEventVO> listEvents(String todoId, String userId);

    /**
     * 批量导入任务。
     */
    TodoImportResultVO importTasks(String userId, java.util.List<TodoImportItemDTO> items);
}

