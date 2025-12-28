package com.pbad.announcement.mapper;

import com.pbad.announcement.domain.po.AnnouncementReadPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementReadMapper {

    int insert(AnnouncementReadPO readRecord);

    AnnouncementReadPO selectByAnnouncementAndUser(@Param("announcementId") String announcementId,
                                                   @Param("userId") String userId);

    List<AnnouncementReadPO> selectByAnnouncementId(@Param("announcementId") String announcementId);

    long countByAnnouncementId(@Param("announcementId") String announcementId);
}

