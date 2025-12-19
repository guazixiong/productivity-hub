package com.pbad.todo.mapper;

import com.pbad.todo.domain.po.TodoModulePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TodoModuleMapper {

    List<TodoModulePO> selectByUser(@Param("userId") String userId);

    TodoModulePO selectById(@Param("id") String id, @Param("userId") String userId);

    int insert(TodoModulePO module);

    int update(TodoModulePO module);

    int delete(@Param("id") String id, @Param("userId") String userId);

    int countTasksByModule(@Param("moduleId") String moduleId, @Param("userId") String userId);
}

