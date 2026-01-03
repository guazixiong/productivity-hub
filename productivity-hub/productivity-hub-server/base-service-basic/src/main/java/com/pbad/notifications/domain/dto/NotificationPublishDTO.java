package com.pbad.notifications.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 对内暴露的通知发布 DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPublishDTO {

    /**
     * 目标用户 ID.
     */
    private String userId;

    /**
     * 通知标题.
     */
    private String title;

    /**
     * 通知内容.
     */
    private String content;

    /**
     * 点击后跳转路径（可选）.
     */
    private String path;

    /**
     * 附加数据（会序列化为 JSON）.
     */
    private Map<String, Object> extra;
}


