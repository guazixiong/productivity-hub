-- 检查并修复 sys_image 表的 share_expires_at 字段类型
-- 如果字段类型不是 DATETIME 或 TIMESTAMP，执行以下语句修复

-- 1. 首先检查字段类型（执行此查询查看字段信息）
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME = 'sys_image' 
--   AND COLUMN_NAME = 'share_expires_at';

-- 2. 如果字段类型不是 DATETIME 或 TIMESTAMP，执行以下语句修改字段类型
-- ALTER TABLE sys_image MODIFY COLUMN share_expires_at DATETIME NULL COMMENT '分享链接过期时间';

