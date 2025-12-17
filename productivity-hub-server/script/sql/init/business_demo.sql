-- 代码生成器 - 公司模板表
DROP TABLE IF EXISTS code_generator_company_template;
CREATE TABLE `code_generator_company_template`
(
    `id`             varchar(64)  NOT NULL COMMENT '模板ID',
    `name`           varchar(100) NOT NULL COMMENT '模板名称',
    `description`    varchar(500) DEFAULT NULL COMMENT '模板描述',
    `base_package`   varchar(200) NOT NULL COMMENT '基础包名',
    `author`         varchar(50)  DEFAULT NULL COMMENT '作者',
    `templates_json` longtext     NOT NULL COMMENT '文件模板列表（JSON格式）',
    `created_at`     varchar(20)  NOT NULL COMMENT '创建时间',
    `updated_at`     varchar(20)  NOT NULL COMMENT '更新时间',
    `created_by`     varchar(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by`     varchar(50)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成器-公司模板表';

-- 代码生成器 - 数据库配置表
DROP TABLE IF EXISTS code_generator_database_config;
CREATE TABLE `code_generator_database_config`
(
    `id`         varchar(64)  NOT NULL COMMENT '配置ID',
    `name`       varchar(100) NOT NULL COMMENT '配置名称',
    `type`       varchar(20)  NOT NULL COMMENT '数据库类型（mysql, postgresql, oracle, sqlserver, sqlite）',
    `host`       varchar(100) NOT NULL COMMENT '主机地址',
    `port`       int(11)      NOT NULL COMMENT '端口',
    `database`   varchar(100) NOT NULL COMMENT '数据库名',
    `schema`     varchar(100) DEFAULT NULL COMMENT 'Schema（PostgreSQL等使用）',
    `username`   varchar(50)  NOT NULL COMMENT '用户名',
    `password`   varchar(200) NOT NULL COMMENT '密码（加密存储）',
    `created_at` varchar(20)  NOT NULL COMMENT '创建时间',
    `updated_at` varchar(20)  NOT NULL COMMENT '更新时间',
    `created_by` varchar(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by` varchar(50)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成器-数据库配置表';

-- 宝藏类网址 - 标签表
DROP TABLE IF EXISTS bookmark_tag;
CREATE TABLE `bookmark_tag`
(
    `id`         varchar(64)  NOT NULL COMMENT '标签ID',
    `name`       varchar(100) NOT NULL COMMENT '标签名称',
    `parent_id`  varchar(64)  DEFAULT NULL COMMENT '父标签ID（一级标签为NULL，二级标签指向一级标签）',
    `level`      tinyint(1)   NOT NULL COMMENT '标签层级（1=一级标签，2=二级标签）',
    `sort_order` int(11)      DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
    `created_at` varchar(20)  NOT NULL COMMENT '创建时间',
    `updated_at` varchar(20)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宝藏类网址-标签表';

-- 宝藏类网址 - 网址表
DROP TABLE IF EXISTS bookmark_url;
CREATE TABLE `bookmark_url`
(
    `id`          varchar(64)   NOT NULL COMMENT '网址ID',
    `title`       varchar(200)  NOT NULL COMMENT '网址标题',
    `url`         varchar(1000) NOT NULL COMMENT '跳转URL',
    `icon_url`    varchar(500)  DEFAULT NULL COMMENT '网站图标URL',
    `description` varchar(1000) DEFAULT NULL COMMENT '网址描述',
    `created_at`  varchar(20)   NOT NULL COMMENT '创建时间',
    `updated_at`  varchar(20)   NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宝藏类网址-网址表';

-- 宝藏类网址 - 标签网址关联表
DROP TABLE IF EXISTS bookmark_url_tag;
CREATE TABLE `bookmark_url_tag`
(
    `id`        varchar(64) NOT NULL COMMENT '关联ID',
    `url_id`    varchar(64) NOT NULL COMMENT '网址ID',
    `tag_id`    varchar(64) NOT NULL COMMENT '标签ID',
    `created_at` varchar(20) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_url_tag` (`url_id`, `tag_id`),
    KEY `idx_url_id` (`url_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宝藏类网址-标签网址关联表';