package com.pbad.todo.service;

import com.pbad.todo.domain.dto.TodoModuleCreateDTO;
import com.pbad.todo.domain.dto.TodoModuleUpdateDTO;
import com.pbad.todo.domain.vo.TodoModuleVO;

import java.util.List;

public interface TodoModuleService {
    List<TodoModuleVO> listModules(String userId);

    TodoModuleVO createModule(TodoModuleCreateDTO dto, String userId);

    TodoModuleVO updateModule(TodoModuleUpdateDTO dto, String userId);

    void deleteModule(String id, String userId);
}

