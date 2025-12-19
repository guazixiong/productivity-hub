package com.pbad.notifications.mapper;

import com.pbad.notifications.domain.po.NotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(NotificationPO notification);

    List<NotificationPO> selectPageByUser(@Param("userId") String userId,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);

    long countByUser(@Param("userId") String userId);

    int markRead(@Param("id") String id, @Param("userId") String userId);
}


