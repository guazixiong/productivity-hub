-- 为 asset_category 表添加 parent_id 和 level 字段，支持二级分类结构
-- 执行时间：2025-01-XX

-- 添加 parent_id 字段（父分类ID，为空表示大分类，有值表示小分类）
ALTER TABLE asset_category 
ADD COLUMN parent_id VARCHAR(64) NULL COMMENT '父分类ID（为空表示大分类，有值表示小分类）' AFTER icon;

-- 添加 level 字段（分类级别，1表示大分类，2表示小分类）
ALTER TABLE asset_category 
ADD COLUMN level INT NULL COMMENT '分类级别（1表示大分类，2表示小分类）' AFTER parent_id;

-- 添加索引，优化根据父分类ID查询子分类的性能
CREATE INDEX idx_parent_id ON asset_category(parent_id);

-- 更新现有数据：将所有现有分类设置为大分类（level=1）
UPDATE asset_category SET level = 1 WHERE level IS NULL;

-- 注意：如果表中已有数据，需要根据业务需求手动设置 parent_id 和 level
-- 如果希望保留现有分类为一级分类，可以执行：
-- UPDATE asset_category SET level = 1, parent_id = NULL WHERE level IS NULL;

