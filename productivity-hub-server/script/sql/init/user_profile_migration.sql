-- 用户个人信息字段迁移脚本
-- 为 sys_user 表添加 avatar, bio, phone 字段

ALTER TABLE `sys_user`
ADD COLUMN `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL' AFTER `email`,
ADD COLUMN `bio` varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER `avatar`,
ADD COLUMN `phone` varchar(20) DEFAULT NULL COMMENT '手机号' AFTER `bio`;

-- 添加手机号唯一索引（可选，如果需要手机号唯一的话）
-- ALTER TABLE `sys_user` ADD UNIQUE KEY `uk_phone` (`phone`);

