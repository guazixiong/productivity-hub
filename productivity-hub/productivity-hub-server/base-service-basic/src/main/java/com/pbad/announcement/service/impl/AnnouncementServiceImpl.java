package com.pbad.announcement.service.impl;

import com.pbad.announcement.domain.dto.AnnouncementCreateDTO;
import com.pbad.announcement.domain.dto.AnnouncementUpdateDTO;
import com.pbad.announcement.domain.po.AnnouncementPO;
import com.pbad.announcement.domain.po.AnnouncementReadPO;
import com.pbad.announcement.domain.vo.AnnouncementVO;
import com.pbad.announcement.domain.vo.AnnouncementStatsVO;
import com.pbad.announcement.mapper.AnnouncementMapper;
import com.pbad.announcement.mapper.AnnouncementReadMapper;
import com.pbad.announcement.service.AnnouncementService;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.notifications.domain.dto.NotificationPublishDTO;
import com.pbad.notifications.service.NotificationService;
import common.core.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadMapper announcementReadMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public AnnouncementVO create(AnnouncementCreateDTO dto, String creatorId) {
        String now = DATE_FORMAT.format(new Date());
        AnnouncementPO po = new AnnouncementPO();
        po.setId(idGeneratorApi.generateId());
        po.setTitle(dto.getTitle());
        po.setContent(dto.getContent());
        po.setRichContent(dto.getRichContent());
        po.setLink(dto.getLink());
        po.setType(dto.getType() != null ? dto.getType() : "NORMAL");
        po.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        po.setStatus("DRAFT");
        po.setPushStrategy(dto.getPushStrategy() != null ? dto.getPushStrategy() : "LOGIN");
        po.setRequireConfirm(dto.getRequireConfirm() != null ? dto.getRequireConfirm() : 0);
        po.setEffectiveTime(dto.getEffectiveTime());
        po.setExpireTime(dto.getExpireTime());
        po.setScheduledTime(dto.getScheduledTime());
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        announcementMapper.insert(po);
        return toVO(po, null);
    }

    @Override
    public AnnouncementVO update(String id, AnnouncementUpdateDTO dto) {
        AnnouncementPO po = announcementMapper.selectById(id);
        if (po == null) {
            throw new RuntimeException("公告不存在");
        }

        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getContent() != null) po.setContent(dto.getContent());
        if (dto.getRichContent() != null) po.setRichContent(dto.getRichContent());
        if (dto.getLink() != null) po.setLink(dto.getLink());
        if (dto.getType() != null) po.setType(dto.getType());
        if (dto.getPriority() != null) po.setPriority(dto.getPriority());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getPushStrategy() != null) po.setPushStrategy(dto.getPushStrategy());
        if (dto.getRequireConfirm() != null) po.setRequireConfirm(dto.getRequireConfirm());
        if (dto.getEffectiveTime() != null) po.setEffectiveTime(dto.getEffectiveTime());
        if (dto.getExpireTime() != null) po.setExpireTime(dto.getExpireTime());
        if (dto.getScheduledTime() != null) po.setScheduledTime(dto.getScheduledTime());

        po.setUpdatedAt(DATE_FORMAT.format(new Date()));
        announcementMapper.update(po);

        // 如果状态改为PUBLISHED且推送策略为IMMEDIATE，立即推送
        if ("PUBLISHED".equals(po.getStatus()) && "IMMEDIATE".equals(po.getPushStrategy())) {
            pushToAllUsers(po);
        }

        return toVO(po, null);
    }

    @Override
    public void delete(String id) {
        announcementMapper.deleteById(id);
        // 删除相关的阅读记录（可选，也可以保留历史记录）
    }

    @Override
    public AnnouncementVO publish(String id) {
        AnnouncementPO po = announcementMapper.selectById(id);
        if (po == null) {
            throw new RuntimeException("公告不存在");
        }

        po.setStatus("PUBLISHED");
        po.setUpdatedAt(DATE_FORMAT.format(new Date()));
        announcementMapper.update(po);

        // 如果推送策略为IMMEDIATE，立即推送给所有用户
        if ("IMMEDIATE".equals(po.getPushStrategy())) {
            pushToAllUsers(po);
        }

        return toVO(po, null);
    }

    @Override
    public AnnouncementVO withdraw(String id) {
        AnnouncementPO po = announcementMapper.selectById(id);
        if (po == null) {
            throw new RuntimeException("公告不存在");
        }

        po.setStatus("WITHDRAWN");
        po.setUpdatedAt(DATE_FORMAT.format(new Date()));
        announcementMapper.update(po);

        return toVO(po, null);
    }

    @Override
    public AnnouncementVO getById(String id) {
        AnnouncementPO po = announcementMapper.selectById(id);
        if (po == null) {
            return null;
        }
        return toVO(po, null);
    }

    @Override
    public PageResult<AnnouncementVO> page(int pageNum, int pageSize, String status) {
        if (pageNum <= 0) pageNum = 1;
        if (pageSize <= 0) pageSize = 10;

        int offset = (pageNum - 1) * pageSize;
        List<AnnouncementPO> rows = announcementMapper.selectPage(offset, pageSize, status);
        long total = announcementMapper.count(status);

        List<AnnouncementVO> items = CollectionUtils.isEmpty(rows)
                ? Collections.emptyList()
                : rows.stream().map(po -> toVO(po, null)).collect(Collectors.toList());

        PageResult<AnnouncementVO> page = new PageResult<>();
        page.setItems(items);
        page.setTotal(total);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public List<AnnouncementVO> getUnreadByUser(String userId) {
        List<AnnouncementPO> pos = announcementMapper.selectUnreadByUser(userId);
        return CollectionUtils.isEmpty(pos)
                ? Collections.emptyList()
                : pos.stream().map(po -> {
                    AnnouncementReadPO readPO = announcementReadMapper.selectByAnnouncementAndUser(po.getId(), userId);
                    return toVO(po, readPO != null);
                }).collect(Collectors.toList());
    }

    @Override
    public void markRead(String announcementId, String userId) {
        AnnouncementReadPO readPO = announcementReadMapper.selectByAnnouncementAndUser(announcementId, userId);
        if (readPO == null) {
            readPO = new AnnouncementReadPO();
            readPO.setId(idGeneratorApi.generateId());
            readPO.setAnnouncementId(announcementId);
            readPO.setUserId(userId);
            readPO.setReadAt(DATE_FORMAT.format(new Date()));
            announcementReadMapper.insert(readPO);
        }
    }

    @Override
    public AnnouncementStatsVO getStats(String announcementId) {
        long totalUsers = userMapper.selectAll().size();
        long readUsers = announcementReadMapper.countByAnnouncementId(announcementId);

        AnnouncementStatsVO stats = new AnnouncementStatsVO();
        stats.setAnnouncementId(announcementId);
        stats.setTotalUsers((long) totalUsers);
        stats.setReadUsers(readUsers);
        stats.setReadRate(totalUsers > 0 ? (readUsers * 100.0 / totalUsers) : 0.0);
        return stats;
    }

    /**
     * 推送给所有用户（立即推送策略）.
     */
    private void pushToAllUsers(AnnouncementPO po) {
        List<com.pbad.auth.domain.po.UserPO> users = userMapper.selectAll();
        for (com.pbad.auth.domain.po.UserPO user : users) {
            NotificationPublishDTO dto = new NotificationPublishDTO();
            dto.setUserId(user.getId());
            dto.setTitle(po.getTitle());
            dto.setContent(po.getContent());
            dto.setPath(po.getLink());
            notificationService.publish(dto);
        }
    }

    private AnnouncementVO toVO(AnnouncementPO po, Boolean read) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        vo.setContent(po.getContent());
        vo.setRichContent(po.getRichContent());
        vo.setLink(po.getLink());
        vo.setType(po.getType());
        vo.setPriority(po.getPriority());
        vo.setStatus(po.getStatus());
        vo.setPushStrategy(po.getPushStrategy());
        vo.setRequireConfirm(po.getRequireConfirm());
        vo.setEffectiveTime(po.getEffectiveTime());
        vo.setExpireTime(po.getExpireTime());
        vo.setScheduledTime(po.getScheduledTime());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        vo.setRead(read);
        return vo;
    }
}

