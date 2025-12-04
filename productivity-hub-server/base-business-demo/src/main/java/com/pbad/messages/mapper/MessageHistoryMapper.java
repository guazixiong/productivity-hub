package com.pbad.messages.mapper;

import com.pbad.messages.domain.po.MessageHistoryPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息推送历史 Mapper 接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface MessageHistoryMapper {

    /**
     * 插入消息历史
     *
     * @param history 消息历史
     * @return 插入行数
     */
    int insert(MessageHistoryPO history);

    /**
     * 分页查询消息历史（按时间倒序）
     *
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @return 消息历史列表
     */
    List<MessageHistoryPO> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计消息历史总数
     *
     * @return 总记录数
     */
    long countAll();
}

