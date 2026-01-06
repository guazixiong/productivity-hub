package com.pbad.announcement.mapper;

import com.pbad.announcement.domain.po.AnnouncementPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    int insert(AnnouncementPO announcement);

    int update(AnnouncementPO announcement);

    int deleteById(@Param("id") String id);

    AnnouncementPO selectById(@Param("id") String id);

    List<AnnouncementPO> selectPage(@Param("offset") int offset,
                                    @Param("pageSize") int pageSize,
                                    @Param("status") String status);

    long count(@Param("status") String status);

    /**
     * 查询有效的未读公告（用户查看时使用）.
     * 条件：状态为PUBLISHED，在有效期内，用户未读
     */
    List<AnnouncementPO> selectUnreadByUser(@Param("userId") String userId);

    /**
     * 查询所有有效的公告（用于立即推送）.
     */
    List<AnnouncementPO> selectActiveAnnouncements();
}

