-- ============================================
-- 资产分类表建表语句及初始数据
-- 创建时间：2025-01-XX
-- 说明：创建 asset_category 表，并插入常用的大分类和小分类数据
-- ============================================

-- 删除表（如果存在）
DROP TABLE IF EXISTS asset_category;

-- 创建资产分类表
CREATE TABLE asset_category (
    id VARCHAR(64) PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) COMMENT '分类图标',
    parent_id VARCHAR(64) NULL COMMENT '父分类ID（为空表示大分类，有值表示小分类）',
    level INT NOT NULL COMMENT '分类级别（1表示大分类，2表示小分类）',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认分类',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id) COMMENT '父分类ID索引',
    INDEX idx_level (level) COMMENT '分类级别索引',
    INDEX idx_sort_order (sort_order) COMMENT '排序顺序索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产分类表';

-- ============================================
-- 插入大分类数据（level=1）
-- ============================================

-- 电子产品
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001', '电子产品', 'Monitor', NULL, 1, FALSE, 1, NOW(), NOW());

-- 家具家电
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002', '家具家电', 'HomeFilled', NULL, 1, FALSE, 2, NOW(), NOW());

-- 服装配饰
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003', '服装配饰', 'T-Shirt', NULL, 1, FALSE, 3, NOW(), NOW());

-- 运动健身
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004', '运动健身', 'Basketball', NULL, 1, FALSE, 4, NOW(), NOW());

-- 书籍文具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_005', '书籍文具', 'Reading', NULL, 1, FALSE, 5, NOW(), NOW());

-- 交通工具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006', '交通工具', 'Van', NULL, 1, FALSE, 6, NOW(), NOW());

-- 其他
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_007', '其他', 'More', NULL, 1, FALSE, 99, NOW(), NOW());

-- ============================================
-- 插入小分类数据（level=2）
-- ============================================

-- 电子产品 - 手机
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_001', '手机', 'Iphone', 'cat_001', 2, FALSE, 1, NOW(), NOW());

-- 电子产品 - 电脑
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_002', '电脑', 'Monitor', 'cat_001', 2, FALSE, 2, NOW(), NOW());

-- 电子产品 - 平板
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_003', '平板', 'Ipad', 'cat_001', 2, FALSE, 3, NOW(), NOW());

-- 电子产品 - 耳机音响
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_004', '耳机音响', 'Headset', 'cat_001', 2, FALSE, 4, NOW(), NOW());

-- 电子产品 - 相机
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_005', '相机', 'Camera', 'cat_001', 2, FALSE, 5, NOW(), NOW());

-- 电子产品 - 智能手表
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_006', '智能手表', 'Watch', 'cat_001', 2, FALSE, 6, NOW(), NOW());

-- 电子产品 - 其他电子设备
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_001_007', '其他电子设备', 'More', 'cat_001', 2, FALSE, 99, NOW(), NOW());

-- 家具家电 - 沙发
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_001', '沙发', 'HomeFilled', 'cat_002', 2, FALSE, 1, NOW(), NOW());

-- 家具家电 - 床具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_002', '床具', 'HomeFilled', 'cat_002', 2, FALSE, 2, NOW(), NOW());

-- 家具家电 - 桌椅
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_003', '桌椅', 'HomeFilled', 'cat_002', 2, FALSE, 3, NOW(), NOW());

-- 家具家电 - 电视
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_004', '电视', 'Monitor', 'cat_002', 2, FALSE, 4, NOW(), NOW());

-- 家具家电 - 冰箱
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_005', '冰箱', 'HomeFilled', 'cat_002', 2, FALSE, 5, NOW(), NOW());

-- 家具家电 - 洗衣机
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_006', '洗衣机', 'HomeFilled', 'cat_002', 2, FALSE, 6, NOW(), NOW());

-- 家具家电 - 空调
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_007', '空调', 'HomeFilled', 'cat_002', 2, FALSE, 7, NOW(), NOW());

-- 家具家电 - 其他家具家电
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_002_008', '其他家具家电', 'More', 'cat_002', 2, FALSE, 99, NOW(), NOW());

-- 服装配饰 - 上衣
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_001', '上衣', 'T-Shirt', 'cat_003', 2, FALSE, 1, NOW(), NOW());

-- 服装配饰 - 下装
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_002', '下装', 'T-Shirt', 'cat_003', 2, FALSE, 2, NOW(), NOW());

-- 服装配饰 - 鞋子
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_003', '鞋子', 'T-Shirt', 'cat_003', 2, FALSE, 3, NOW(), NOW());

-- 服装配饰 - 包包
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_004', '包包', 'T-Shirt', 'cat_003', 2, FALSE, 4, NOW(), NOW());

-- 服装配饰 - 配饰
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_005', '配饰', 'T-Shirt', 'cat_003', 2, FALSE, 5, NOW(), NOW());

-- 服装配饰 - 其他服装
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_003_006', '其他服装', 'More', 'cat_003', 2, FALSE, 99, NOW(), NOW());

-- 运动健身 - 运动鞋
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004_001', '运动鞋', 'Basketball', 'cat_004', 2, FALSE, 1, NOW(), NOW());

-- 运动健身 - 运动服
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004_002', '运动服', 'Basketball', 'cat_004', 2, FALSE, 2, NOW(), NOW());

-- 运动健身 - 健身器材
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004_003', '健身器材', 'Basketball', 'cat_004', 2, FALSE, 3, NOW(), NOW());

-- 运动健身 - 球类
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004_004', '球类', 'Basketball', 'cat_004', 2, FALSE, 4, NOW(), NOW());

-- 运动健身 - 其他运动用品
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_004_005', '其他运动用品', 'More', 'cat_004', 2, FALSE, 99, NOW(), NOW());

-- 书籍文具 - 书籍
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_005_001', '书籍', 'Reading', 'cat_005', 2, FALSE, 1, NOW(), NOW());

-- 书籍文具 - 文具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_005_002', '文具', 'Reading', 'cat_005', 2, FALSE, 2, NOW(), NOW());

-- 书籍文具 - 电子阅读器
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_005_003', '电子阅读器', 'Reading', 'cat_005', 2, FALSE, 3, NOW(), NOW());

-- 书籍文具 - 其他书籍文具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_005_004', '其他书籍文具', 'More', 'cat_005', 2, FALSE, 99, NOW(), NOW());

-- 交通工具 - 汽车
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006_001', '汽车', 'Van', 'cat_006', 2, FALSE, 1, NOW(), NOW());

-- 交通工具 - 摩托车
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006_002', '摩托车', 'Van', 'cat_006', 2, FALSE, 2, NOW(), NOW());

-- 交通工具 - 电动车
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006_003', '电动车', 'Van', 'cat_006', 2, FALSE, 3, NOW(), NOW());

-- 交通工具 - 自行车
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006_004', '自行车', 'Van', 'cat_006', 2, FALSE, 4, NOW(), NOW());

-- 交通工具 - 其他交通工具
INSERT INTO asset_category (id, name, icon, parent_id, level, is_default, sort_order, created_at, updated_at)
VALUES ('cat_006_005', '其他交通工具', 'More', 'cat_006', 2, FALSE, 99, NOW(), NOW());

-- ============================================
-- 数据插入完成
-- ============================================
-- 说明：
-- 1. 大分类（level=1）共7个：电子产品、家具家电、服装配饰、运动健身、书籍文具、交通工具、其他
-- 2. 小分类（level=2）共40个，分布在各个大分类下
-- 3. 所有分类的ID采用 cat_XXX 格式，便于识别和管理
-- 4. 图标字段使用 Element Plus 图标名称（如 Monitor、HomeFilled 等）
-- 5. 可以根据实际需求修改图标名称或添加更多分类
-- ============================================

