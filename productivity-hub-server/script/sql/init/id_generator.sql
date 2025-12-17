--- 发号器
CREATE TABLE `id_generator_info`
(
    `module_key`     varchar(20) NOT NULL COMMENT '模块唯一标识',
    `module_name`    varchar(50)  DEFAULT NULL COMMENT '模块名称',
    `worker_id`      bigint(32) DEFAULT NULL COMMENT '工作ID',
    `data_center_id` bigint(32) DEFAULT NULL COMMENT '数据中心ID',
    `status`         char(1)      DEFAULT NULL COMMENT '状态',
    `remark`         varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`module_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;