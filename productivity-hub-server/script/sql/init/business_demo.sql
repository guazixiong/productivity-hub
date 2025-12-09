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