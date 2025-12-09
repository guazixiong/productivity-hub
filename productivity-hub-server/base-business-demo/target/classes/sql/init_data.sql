-- Productivity Hub 数据库表结构

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `name` VARCHAR(100) DEFAULT NULL COMMENT '姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `roles` VARCHAR(255) DEFAULT NULL COMMENT '角色（JSON数组）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 配置项表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` VARCHAR(64) NOT NULL COMMENT '配置ID',
  `module` VARCHAR(50) NOT NULL COMMENT '所属模块（auth, sendgrid, dingtalk, resend, agents, home, dify）',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名（支持点号分隔）',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `created_at` VARCHAR(19) DEFAULT NULL COMMENT '创建时间（YYYY-MM-DD HH:mm）',
  `updated_at` VARCHAR(19) DEFAULT NULL COMMENT '更新时间（YYYY-MM-DD HH:mm）',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_key` (`module`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置项表';

-- 消息推送历史表
CREATE TABLE IF NOT EXISTS `sys_message_history` (
  `id` VARCHAR(64) NOT NULL COMMENT '历史记录ID',
  `channel` VARCHAR(20) NOT NULL COMMENT '推送渠道（sendgrid, dingtalk, resend）',
  `request_data` TEXT DEFAULT NULL COMMENT '原始请求参数（JSON）',
  `status` VARCHAR(20) NOT NULL COMMENT '发送状态（success, failed）',
  `response_data` TEXT DEFAULT NULL COMMENT '第三方服务响应内容',
  `created_at` VARCHAR(19) DEFAULT NULL COMMENT '创建时间（YYYY-MM-DD HH:mm）',
  PRIMARY KEY (`id`),
  KEY `idx_channel_created` (`channel`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息推送历史表';

-- 智能体表
CREATE TABLE IF NOT EXISTS `sys_agent` (
  `id` VARCHAR(64) NOT NULL COMMENT '智能体ID',
  `name` VARCHAR(100) NOT NULL COMMENT '智能体名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '智能体描述',
  `version` VARCHAR(20) DEFAULT NULL COMMENT '版本号',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签列表（JSON数组）',
  `latency_ms` INT DEFAULT 0 COMMENT '平均延迟（毫秒）',
  `owner` VARCHAR(100) DEFAULT NULL COMMENT '负责人/团队',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体表';

-- 智能体调用日志表
CREATE TABLE IF NOT EXISTS `sys_agent_log` (
  `id` VARCHAR(64) NOT NULL COMMENT '日志ID',
  `agent_id` VARCHAR(64) NOT NULL COMMENT '智能体ID',
  `agent_name` VARCHAR(100) DEFAULT NULL COMMENT '智能体名称',
  `status` VARCHAR(20) NOT NULL COMMENT '状态（success, failed, running）',
  `duration` INT DEFAULT 0 COMMENT '耗时（毫秒）',
  `input_data` TEXT DEFAULT NULL COMMENT '输入内容（JSON）',
  `output_data` TEXT DEFAULT NULL COMMENT '输出内容',
  `created_at` VARCHAR(19) DEFAULT NULL COMMENT '创建时间（YYYY-MM-DD HH:mm）',
  PRIMARY KEY (`id`),
  KEY `idx_agent_created` (`agent_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体调用日志表';

-- 工具统计表
CREATE TABLE IF NOT EXISTS `sys_tool_stat` (
  `id` VARCHAR(64) NOT NULL COMMENT '工具ID',
  `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `clicks` INT DEFAULT 0 COMMENT '点击次数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tool_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具统计表';

-- Productivity Hub 初始化数据

-- 初始化用户数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `email`, `roles`) VALUES
('u_admin', 'admin', 'admin123', '超级管理员', 'admin@productivity-hub.io', '["admin"]'),
('u_ops', 'ops', 'ops123', '运营', 'ops@productivity-hub.io', '["ops"]')
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 初始化配置项数据
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_1', 'auth', 'token.expireDays', '7', '登录态 Token 过期时间（单位：天）', '2025-11-20 09:00', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_2', 'auth', '_module.description', '权限相关', '模块描述', '2025-12-05 11:05', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_3', 'dingtalk', 'dingtalk.webhook', 'https://oapi.dingtalk.com/robot/send?access_token=e806006cafe10887a82d66543ff4cb3d4dbc82deb12cd4f7cf81fad1e3e7a931', '钉钉 Webhook 地址', '2025-11-20 09:00', '2025-12-05 15:21', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_4', 'dingtalk', '_module.description', '钉钉消息推送\n', '模块描述', '2025-12-05 11:05', '2025-12-05 15:21', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_5', 'dingtalk', 'dingtalk.sign', 'SEC4aa9f657ac9a32a69645b6a7cde94c3b45bd7252ff87725383c5a741c092166a', '钉钉加签秘钥（为空则不启用签名）', '2025-11-20 09:00', '2025-12-05 15:21', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_6', 'home', 'offWorkTime', '18:00', '下班时间', '2025-11-20 09:00', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_7', 'home', 'lunchBreakTime', '11:30', '午休时间', '2025-11-20 09:00', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_8', 'home', '_module.description', '首页模块', '模块描述', '2025-12-05 11:05', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_16', 'home', 'salaryPayDay', '15', '薪资发放日（每月的第几天，节假日顺延）', '2025-11-20 09:00', '2025-12-08 09:00', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_9', 'resend', 'resend.apiKey', 're_UQSg8xzv_3Q5q94H9w6w4JAjXxx9e9xJP', 'Resend API Key（对应唯一的收件人邮箱）', '2025-11-20 09:00', '2025-12-05 14:54', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_10', 'resend', 'resend.fromEmail', 'onboarding@resend.dev', 'Resend 发件人邮箱地址（Resend平台）', '2025-11-20 09:00', '2025-12-05 14:54', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_11', 'resend', 'resend.toEmail', 'pbad0606@163.com', 'Resend 收件人邮箱地址（与 apiKey 对应，仅作展示）', '2025-11-20 09:00', '2025-12-05 14:54', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_12', 'resend', '_module.description', 'Resend邮件配置', '模块描述', '2025-12-05 11:05', '2025-12-05 14:54', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_13', 'sendgrid', 'sendgrid.apiKey', '123456789', 'SendGrid API Key（请在此填写真实的 API Key，勿提交到代码仓库）', '2025-11-20 09:00', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_14', 'sendgrid', 'sendgrid.fromEmail', 'pbad0606@163.com', 'SendGrid 发件人邮箱地址', '2025-11-20 09:00', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_15', 'sendgrid', '_module.description', 'Sendgrid邮件配置\n', '模块描述', '2025-12-05 11:05', '2025-12-05 11:05', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_17', 'dify', 'api.url', '', 'Dify API 的 URL（例如：https://api.dify.ai）', '2025-12-20 10:00', '2025-12-20 10:00', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_18', 'dify', 'api.key', '', 'Dify API 的 Key（请在此填写真实的 API Key，勿提交到代码仓库）', '2025-12-20 10:00', '2025-12-20 10:00', 'admin');
INSERT INTO `productivity_hub`.`sys_config`(`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES ('cfg_19', 'dify', '_module.description', 'Dify AI 聊天助手配置', '模块描述', '2025-12-20 10:00', '2025-12-20 10:00', 'admin');

-- 初始化智能体数据
INSERT INTO `sys_agent` (`id`, `name`, `description`, `version`, `tags`, `latency_ms`, `owner`) VALUES
('agent-cs-bot', '客服问答助手', '根据 FAQ 与知识库自动回复客户问题', 'v2.4.1', '["客服", "QA"]', 1200, '智能体验组'),
('agent-report', '日报生成器', '自动生成工作日报', 'v1.0.0', '["报告", "自动化"]', 800, '智能体验组'),
('agent-workflow', '流程编排助手', '协助流程编排和优化', 'v1.2.0', '["流程", "编排"]', 1500, '智能体验组')
ON DUPLICATE KEY UPDATE `name`=`name`;

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