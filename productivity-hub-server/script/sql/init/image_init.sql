-- 图片管理模块表
-- 图片信息表 (sys_image)
DROP TABLE IF EXISTS `sys_image`;
CREATE TABLE `sys_image` (
    `id`                  VARCHAR(64)  NOT NULL COMMENT '主键ID',
    `user_id`             VARCHAR(64)  NOT NULL COMMENT '用户ID，用于用户数据隔离',
    `original_filename`   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_filename`     VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID + 扩展名）',
    `file_path`           VARCHAR(512) NOT NULL COMMENT '文件存储路径（相对路径）',
    `file_url`            VARCHAR(512) NOT NULL COMMENT '文件访问URL',
    `file_size`           BIGINT       NOT NULL COMMENT '文件大小（单位：字节）',
    `file_type`           VARCHAR(50)  NOT NULL COMMENT '文件类型（如：image/jpeg, image/png）',
    `file_extension`      VARCHAR(10)  NOT NULL COMMENT '文件扩展名（如：jpg, png, gif）',
    `width`               INT                   DEFAULT NULL COMMENT '图片宽度（单位：像素）',
    `height`              INT                   DEFAULT NULL COMMENT '图片高度（单位：像素）',
    `category`            VARCHAR(50)  NOT NULL COMMENT '图片分类（avatar、bookmark、todo、health、article、other）',
    `business_module`     VARCHAR(50)           DEFAULT NULL COMMENT '业务模块标识',
    `business_id`         VARCHAR(64)           DEFAULT NULL COMMENT '业务关联ID',
    `description`         TEXT                  DEFAULT NULL COMMENT '图片描述',
    `thumbnail_path`      VARCHAR(512)          DEFAULT NULL COMMENT '缩略图存储路径（相对路径）',
    `thumbnail_url`       VARCHAR(512)          DEFAULT NULL COMMENT '缩略图访问URL',
    `share_token`         VARCHAR(64)           DEFAULT NULL COMMENT '分享令牌（用于图片分享功能）',
    `share_expires_at`    DATETIME              DEFAULT NULL COMMENT '分享链接过期时间（为空表示永久有效）',
    `access_count`        BIGINT       NOT NULL DEFAULT 0 COMMENT '访问次数',
    `status`              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE、DELETED、ARCHIVED）',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stored_filename` (`stored_filename`),
    UNIQUE KEY `uk_share_token` (`share_token`),
    KEY `idx_user_category` (`user_id`, `category`),
    KEY `idx_user_business` (`user_id`, `business_module`, `business_id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_category` (`category`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片信息表';

