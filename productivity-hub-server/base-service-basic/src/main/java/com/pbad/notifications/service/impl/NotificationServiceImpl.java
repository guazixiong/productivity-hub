package com.pbad.notifications.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.notifications.domain.dto.NotificationPublishDTO;
import com.pbad.notifications.domain.po.NotificationPO;
import com.pbad.notifications.domain.vo.NotificationVO;
import com.pbad.notifications.mapper.NotificationMapper;
import com.pbad.websocket.api.WebSocketApi;
import com.pbad.websocket.model.MessageDTO;
import common.core.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements com.pbad.notifications.service.NotificationService {

    private final NotificationMapper notificationMapper;

    private final WebSocketApi webSocketApi;

    private final ObjectMapper objectMapper;

    private final IdGeneratorApi idGeneratorApi;

    private static final String WS_TYPE_NOTIFICATION = "notification";

    private static final String CLIENT_WEB = "web";

    @Override
    public void publish(NotificationPublishDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            return;
        }
        // 1. 写入数据库
        NotificationPO po = new NotificationPO();
        String notificationId = idGeneratorApi.generateId();
        po.setId(notificationId);
        po.setUserId(dto.getUserId());
        po.setTitle(dto.getTitle());
        po.setContent(dto.getContent());
        po.setLink(dto.getPath());
        po.setReadFlag(0);
        po.setExtraData(toJsonSafe(dto.getExtra()));
        notificationMapper.insert(po);

        // 2. 组装 WebSocket 消息并推送（包含通知 ID）
        Map<String, Object> payload = buildWsPayload(dto, notificationId);
        String messageJson = toJsonSafe(payload);

        MessageDTO messageDTO = new MessageDTO()
                .setUserAccount(dto.getUserId())
                .setClient(CLIENT_WEB)
                .setType(WS_TYPE_NOTIFICATION)
                .setMessage(messageJson);

        webSocketApi.sendMessageToClientUser(messageDTO);
    }

    @Override
    public PageResult<NotificationVO> pageByCurrentUser(int pageNum, int pageSize, String userId) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        int offset = (pageNum - 1) * pageSize;
        List<NotificationPO> rows = notificationMapper.selectPageByUser(userId, offset, pageSize);
        long total = notificationMapper.countByUser(userId);

        List<NotificationVO> items = CollectionUtils.isEmpty(rows)
                ? Collections.emptyList()
                : rows.stream().map(this::toVO).collect(Collectors.toList());

        PageResult<NotificationVO> page = new PageResult<>();
        page.setItems(items);
        page.setTotal(total);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public void markRead(String id, String userId) {
        if (id == null || userId == null) {
            return;
        }
        notificationMapper.markRead(id, userId);
    }

    private NotificationVO toVO(NotificationPO po) {
        NotificationVO vo = new NotificationVO();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        vo.setContent(po.getContent());
        vo.setLink(po.getLink());
        vo.setRead(po.getReadFlag() != null && po.getReadFlag() == 1);
        vo.setCreatedAt(po.getCreatedAt());
        vo.setExtra(parseJsonObject(po.getExtraData()));
        return vo;
    }

    private Map<String, Object> buildWsPayload(NotificationPublishDTO dto, String notificationId) {
        Map<String, Object> extra = dto.getExtra() == null ? Collections.emptyMap() : dto.getExtra();
        return new java.util.LinkedHashMap<String, Object>() {{
            put("type", WS_TYPE_NOTIFICATION);
            put("id", notificationId);
            put("title", dto.getTitle());
            put("content", dto.getContent());
            put("path", dto.getPath());
            put("extra", extra);
        }};
    }

    private String toJsonSafe(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseJsonObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}


