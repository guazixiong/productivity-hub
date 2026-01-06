package com.pbad.todo.mapper;

import com.pbad.todo.domain.po.TodoEventPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TodoEventMapper {

    int insertEvent(TodoEventPO event);

    List<TodoEventPO> selectByTodoId(@Param("todoId") String todoId, @Param("userId") String userId);
}

