-- ----------------------------
-- 健康状况模块数据库表结构
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for health_training_plan
-- ----------------------------
DROP TABLE IF EXISTS `health_training_plan`;
CREATE TABLE `health_training_plan` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `plan_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划名称',
  `plan_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划类型（减脂、增肌、塑形、耐力提升、康复训练、其他）',
  `target_duration_days` int(11) NULL DEFAULT NULL COMMENT '目标持续天数',
  `target_calories_per_day` int(11) NULL DEFAULT NULL COMMENT '每日目标卡路里',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '计划描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE-进行中、COMPLETED-已完成、PAUSED-已暂停、CANCELLED-已取消）',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE,
  INDEX `idx_plan_type`(`plan_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '训练计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for health_exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `health_exercise_record`;
CREATE TABLE `health_exercise_record` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID（用于用户数据隔离）',
  `exercise_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运动类型（跑步、游泳、骑行、力量训练、瑜伽、有氧运动、球类运动、其他）',
  `exercise_date` date NOT NULL COMMENT '运动日期',
  `duration_minutes` int(11) NOT NULL COMMENT '运动时长（分钟，范围：1-1440）',
  `calories_burned` int(11) NULL DEFAULT NULL COMMENT '消耗卡路里（单位：千卡）',
  `distance_km` decimal(10,2) NULL DEFAULT NULL COMMENT '运动距离（单位：公里，适用于跑步、骑行等）',
  `heart_rate_avg` int(11) NULL DEFAULT NULL COMMENT '平均心率（单位：次/分钟）',
  `heart_rate_max` int(11) NULL DEFAULT NULL COMMENT '最大心率（单位：次/分钟）',
  `training_plan_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联的训练计划ID（外键）',
  `exercise_action_ref_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '训练动作参考链接（支持多个链接，JSON格式存储）',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_exercise_date`(`exercise_date`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `exercise_date`) USING BTREE,
  INDEX `idx_exercise_type`(`exercise_type`) USING BTREE,
  INDEX `idx_training_plan_id`(`training_plan_id`) USING BTREE,
  CONSTRAINT `fk_exercise_training_plan` FOREIGN KEY (`training_plan_id`) REFERENCES `health_training_plan` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运动记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for health_water_intake
-- ----------------------------
DROP TABLE IF EXISTS `health_water_intake`;
CREATE TABLE `health_water_intake` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `intake_date` date NOT NULL COMMENT '饮水日期',
  `intake_time` time NOT NULL COMMENT '饮水时间（精确到分钟）',
  `water_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '饮水类型（白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他）',
  `volume_ml` int(11) NOT NULL COMMENT '饮水量（单位：毫升，范围：1-5000）',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_intake_date`(`intake_date`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `intake_date`) USING BTREE,
  INDEX `idx_water_type`(`water_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '饮水记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for health_water_target
-- ----------------------------
DROP TABLE IF EXISTS `health_water_target`;
CREATE TABLE `health_water_target` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID（唯一索引）',
  `daily_target_ml` int(11) NOT NULL DEFAULT 2000 COMMENT '每日目标饮水量（单位：毫升，默认：2000，范围：500-10000）',
  `reminder_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用提醒（0-否，1-是）',
  `reminder_intervals` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提醒时间间隔（JSON数组格式，如：["09:00", "12:00", "15:00", "18:00"]）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '饮水目标配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for health_weight_record
-- ----------------------------
DROP TABLE IF EXISTS `health_weight_record`;
CREATE TABLE `health_weight_record` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `record_date` date NOT NULL COMMENT '记录日期',
  `record_time` time NULL DEFAULT NULL COMMENT '记录时间（精确到分钟，用于同一天多次记录）',
  `weight_kg` decimal(5,2) NOT NULL COMMENT '体重（单位：公斤，范围：20.00-300.00）',
  `body_fat_percentage` decimal(5,2) NULL DEFAULT NULL COMMENT '体脂率（单位：%，范围：5.00-50.00）',
  `muscle_mass_kg` decimal(5,2) NULL DEFAULT NULL COMMENT '肌肉量（单位：公斤）',
  `bmi` decimal(4,2) NULL DEFAULT NULL COMMENT 'BMI指数（自动计算：体重(kg) / 身高(m)²）',
  `health_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '健康状态（偏瘦、正常、偏胖、肥胖，根据BMI自动判断）',
  `height_cm` decimal(5,2) NULL DEFAULT NULL COMMENT '身高（单位：厘米，用于计算BMI，范围：100.00-250.00）',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `record_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '体重记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for health_user_body_info
-- ----------------------------
DROP TABLE IF EXISTS `health_user_body_info`;
CREATE TABLE `health_user_body_info` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID（唯一索引）',
  `height_cm` decimal(5,2) NULL DEFAULT NULL COMMENT '身高（单位：厘米）',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期（用于计算年龄）',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别（MALE-男、FEMALE-女、OTHER-其他）',
  `target_weight_kg` decimal(5,2) NULL DEFAULT NULL COMMENT '目标体重（单位：公斤）',
  `resend_email` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Resend邮箱地址（用于接收健康统计日报）',
  `daily_report_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用每日健康统计邮件推送（0-否，1-是）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户身体信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

