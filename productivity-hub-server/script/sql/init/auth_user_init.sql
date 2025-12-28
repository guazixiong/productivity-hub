-- 用户表（认证模块）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`         varchar(64)  NOT NULL COMMENT '用户ID',
    `username`   varchar(64)  NOT NULL COMMENT '登录用户名',
    `password`   varchar(128) NOT NULL COMMENT '登录密码（当前为明文存储）',
    `name`       varchar(128) DEFAULT NULL COMMENT '姓名',
    `email`      varchar(256) DEFAULT NULL COMMENT '邮箱',
    `roles`      text         DEFAULT NULL COMMENT '角色列表(JSON数组字符串)',
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证用户表';

-- 初始化管理员账号（密码为明文 123456）
INSERT INTO `sys_user`(`id`, `username`, `password`, `name`, `email`, `roles`)
VALUES ('admin', 'admin', '123456', 'Administrator', 'admin@example.com', '["admin"]');

