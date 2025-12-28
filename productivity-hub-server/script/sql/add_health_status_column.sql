-- ============================================
-- 为 health_weight_record 表添加 health_status 列
-- 用于存储根据BMI自动判断的健康状态
-- ============================================

SET NAMES utf8mb4;

-- 检查列是否存在，如果不存在则添加
-- 注意：MySQL 5.7+ 支持 IF NOT EXISTS，但为了兼容性，先检查
SET @dbname = DATABASE();
SET @tablename = 'health_weight_record';
SET @columnname = 'health_status';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(20) NULL DEFAULT NULL COMMENT ''健康状态（偏瘦、正常、偏胖、肥胖，根据BMI自动判断）'' AFTER bmi')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 或者直接执行（如果确定列不存在）：
-- ALTER TABLE `health_weight_record` 
-- ADD COLUMN `health_status` VARCHAR(20) NULL DEFAULT NULL COMMENT '健康状态（偏瘦、正常、偏胖、肥胖，根据BMI自动判断）' AFTER `bmi`;

