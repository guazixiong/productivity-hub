package com.pbad.messages.mapper;

import com.pbad.messages.domain.po.MessageHistoryPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息推送历史 Mapper 接口.
 */
public interface MessageHistoryMapper {

    int insert(MessageHistoryPO history);

    List<MessageHistoryPO> selectPage(@Param("userId") String userId,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    long countAll(@Param("userId") String userId);
}


