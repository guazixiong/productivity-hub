-- 短链表初始化脚本
-- 为 sys_short_link 表添加 short_link_url 字段（存储第三方短链服务返回的完整URL）

ALTER TABLE `sys_short_link`
ADD COLUMN `short_link_url` varchar(2000) DEFAULT NULL COMMENT '第三方短链完整URL' AFTER `original_url`;

