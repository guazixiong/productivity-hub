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
  `module` VARCHAR(50) NOT NULL COMMENT '所属模块（auth, sendgrid, dingtalk, agents, home）',
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
  `channel` VARCHAR(20) NOT NULL COMMENT '推送渠道（sendgrid, dingtalk）',
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
INSERT INTO `sys_config` (`id`, `module`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`, `updated_by`) VALUES
('cfg_1', 'auth', 'token.expireDays', '7', '登录态 Token 过期时间（单位：天）', '2025-11-20 09:00', '2025-11-28 10:00', '系统'),
('cfg_6', 'sendgrid', 'sendgrid.apiKey', '', 'SendGrid API Key（请在此填写真实的 API Key，勿提交到代码仓库）', '2025-11-20 09:00', '2025-11-20 09:00', '系统'),
('cfg_7', 'sendgrid', 'sendgrid.fromEmail', 'pbad0606@163.com', 'SendGrid 发件人邮箱地址', '2025-11-20 09:00', '2025-11-20 09:00', '系统'),
('cfg_3', 'dingtalk', 'webhook', 'https://oapi.dingtalk.com/robot/send?access_token=xxx', '钉钉 Webhook 地址', '2025-11-20 09:00', '2025-11-20 09:00', '系统'),
('cfg_4', 'home', 'offWorkTime', '18:00', '下班时间', '2025-11-20 09:00', '2025-11-20 09:00', '系统'),
('cfg_5', 'home', 'lunchBreakTime', '12:00', '午休时间', '2025-11-20 09:00', '2025-11-20 09:00', '系统')
ON DUPLICATE KEY UPDATE `config_value`=`config_value`;

-- 初始化智能体数据
INSERT INTO `sys_agent` (`id`, `name`, `description`, `version`, `tags`, `latency_ms`, `owner`) VALUES
('agent-cs-bot', '客服问答助手', '根据 FAQ 与知识库自动回复客户问题', 'v2.4.1', '["客服", "QA"]', 1200, '智能体验组'),
('agent-report', '日报生成器', '自动生成工作日报', 'v1.0.0', '["报告", "自动化"]', 800, '智能体验组'),
('agent-workflow', '流程编排助手', '协助流程编排和优化', 'v1.2.0', '["流程", "编排"]', 1500, '智能体验组')
ON DUPLICATE KEY UPDATE `name`=`name`;