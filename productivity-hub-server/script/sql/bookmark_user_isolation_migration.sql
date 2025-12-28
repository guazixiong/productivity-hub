-- ============================================
-- 书签模块用户数据隔离迁移脚本
-- 为 bookmark_tag、bookmark_url、bookmark_url_tag 表添加 user_id 字段
-- ============================================

-- 1. 为 bookmark_tag 表添加 user_id 字段
ALTER TABLE `bookmark_tag` 
ADD COLUMN `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID' AFTER `id`;

-- 为现有数据设置默认用户ID（如果有默认用户，请替换为实际用户ID）
-- 注意：这里需要根据实际情况设置，如果已有用户数据，应该根据实际情况分配
-- UPDATE `bookmark_tag` SET `user_id` = 'default_user_id' WHERE `user_id` IS NULL;

-- 添加索引
ALTER TABLE `bookmark_tag` 
ADD INDEX `idx_user_id`(`user_id`) USING BTREE;

-- 2. 为 bookmark_url 表添加 user_id 字段
ALTER TABLE `bookmark_url` 
ADD COLUMN `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID' AFTER `id`;

-- 为现有数据设置默认用户ID（如果有默认用户，请替换为实际用户ID）
-- 注意：这里需要根据实际情况设置，如果已有用户数据，应该根据实际情况分配
-- UPDATE `bookmark_url` SET `user_id` = 'default_user_id' WHERE `user_id` IS NULL;

-- 添加索引
ALTER TABLE `bookmark_url` 
ADD INDEX `idx_user_id`(`user_id`) USING BTREE;

-- 3. bookmark_url_tag 表不需要直接添加 user_id 字段
-- 因为可以通过 url_id 关联到 bookmark_url 表获取 user_id
-- 但为了查询性能，可以考虑添加冗余字段（可选）
-- 这里我们选择不添加，通过 JOIN 查询来保证数据一致性

-- 4. 修改唯一索引，确保同一用户下标签名称和父标签ID的唯一性
-- 先删除旧的唯一性约束（如果存在）
-- 注意：由于原表没有唯一索引约束，这里不需要删除

-- 添加新的唯一索引：同一用户下，同一父标签下，标签名称唯一
ALTER TABLE `bookmark_tag` 
ADD UNIQUE INDEX `uk_user_parent_name`(`user_id`, `parent_id`, `name`) USING BTREE;

-- 5. 修改 bookmark_url_tag 的唯一索引，确保同一用户下 url_id 和 tag_id 的唯一性
-- 由于需要通过 url_id 关联到 bookmark_url 获取 user_id，这里保持原有唯一索引
-- 但需要在应用层确保 url_id 和 tag_id 属于同一用户

