package com.pbad.todo.mapper;

import com.pbad.todo.domain.po.TodoDailyStatsPO;
import com.pbad.todo.domain.po.TodoModuleStatsPO;
import com.pbad.todo.domain.po.TodoTaskPO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoTaskMapper {

    List<TodoTaskPO> selectByUser(@Param("userId") String userId,
                                  @Param("moduleId") String moduleId,
                                  @Param("status") String status);

    List<TodoTaskPO> selectByUserWithPage(@Param("userId") String userId,
                                         @Param("moduleId") String moduleId,
                                         @Param("status") String status,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    Long countByUser(@Param("userId") String userId,
                     @Param("moduleId") String moduleId,
                     @Param("status") String status);

    TodoTaskPO selectById(@Param("id") String id, @Param("userId") String userId);

    List<TodoTaskPO> selectByStatuses(@Param("userId") String userId,
                                      @Param("statuses") List<String> statuses);

    int insertTask(TodoTaskPO task);

    int updateTask(TodoTaskPO task);

    int deleteTask(@Param("id") String id, @Param("userId") String userId);

    List<TodoModuleStatsPO> aggregateByModule(@Param("userId") String userId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    List<TodoDailyStatsPO> aggregateDaily(@Param("userId") String userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    Long countByStatus(@Param("userId") String userId, @Param("status") String status);

    Long sumDuration(@Param("userId") String userId,
                     @Param("start") LocalDateTime start,
                     @Param("end") LocalDateTime end);
}

