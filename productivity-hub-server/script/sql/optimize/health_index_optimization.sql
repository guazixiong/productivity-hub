-- ----------------------------
-- 健康状况模块数据库索引优化脚本
-- 优化查询性能，提升常用查询场景的执行效率
-- ----------------------------
SET NAMES utf8mb4;

-- ----------------------------
-- 1. health_exercise_record 表索引优化
-- ----------------------------

-- 添加复合索引：用于按用户、日期和类型查询（统计查询常用）
ALTER TABLE `health_exercise_record` 
ADD INDEX `idx_user_date_type`(`user_id`, `exercise_date`, `exercise_type`) USING BTREE;

-- 添加复合索引：用于按用户和训练计划查询
ALTER TABLE `health_exercise_record` 
ADD INDEX `idx_user_plan`(`user_id`, `training_plan_id`) USING BTREE;

-- 添加复合索引：用于按用户和日期排序查询（分页查询常用）
-- 注意：MySQL不支持在索引中直接指定DESC，但查询时会自动使用索引
ALTER TABLE `health_exercise_record` 
ADD INDEX `idx_user_date_desc`(`user_id`, `exercise_date`) USING BTREE;

-- ----------------------------
-- 2. health_water_intake 表索引优化
-- ----------------------------

-- 添加复合索引：用于按用户、日期和类型查询（统计查询常用）
ALTER TABLE `health_water_intake` 
ADD INDEX `idx_user_date_type`(`user_id`, `intake_date`, `water_type`) USING BTREE;

-- 添加复合索引：用于按用户、日期和时间排序查询（分页查询常用）
ALTER TABLE `health_water_intake` 
ADD INDEX `idx_user_date_time`(`user_id`, `intake_date`, `intake_time`) USING BTREE;

-- ----------------------------
-- 3. health_weight_record 表索引优化
-- ----------------------------

-- 添加复合索引：用于按用户、日期和时间排序查询（分页查询常用）
ALTER TABLE `health_weight_record` 
ADD INDEX `idx_user_date_time`(`user_id`, `record_date`, `record_time`) USING BTREE;

-- 添加复合索引：用于日期范围查询（统计查询常用）
-- 注意：idx_user_date 已存在，但可以优化为覆盖索引
-- 由于已有 idx_user_date，这里不再重复添加

-- ----------------------------
-- 4. health_training_plan 表索引优化
-- ----------------------------

-- 添加复合索引：用于按用户、状态和创建时间排序查询（分页查询常用）
ALTER TABLE `health_training_plan` 
ADD INDEX `idx_user_status_created`(`user_id`, `status`, `created_at`) USING BTREE;

-- 添加复合索引：用于按用户和日期范围查询
ALTER TABLE `health_training_plan` 
ADD INDEX `idx_user_date_range`(`user_id`, `start_date`, `end_date`) USING BTREE;

-- ----------------------------
-- 索引优化说明
-- ----------------------------
-- 1. 复合索引遵循最左前缀原则，查询条件应尽量使用索引的最左列
-- 2. 索引会增加写入开销，但能显著提升查询性能
-- 3. 定期使用 EXPLAIN 分析查询执行计划，确保索引被正确使用
-- 4. 如果数据量很大，可以考虑分区表进一步优化性能
-- 5. 监控索引使用情况，删除未使用的索引以减少存储开销

