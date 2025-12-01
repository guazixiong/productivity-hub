-- 客商信息
DROP TABLE IF EXISTS mall_customer;
CREATE TABLE `mall_customer`
(
    `customer_id`   bigint(20) NOT NULL COMMENT '客商唯一标识',
    `customer_name` varchar(50) NOT NULL COMMENT '客商名称',
    `phone`         char(20)    NOT NULL COMMENT '联系方式',
    `address`       varchar(255) DEFAULT NULL COMMENT '地址',
    `remark`        varchar(255) DEFAULT NULL COMMENT '备注',
    `create_by`     varchar(50) NOT NULL COMMENT '创建人',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `update_by`     varchar(50) NOT NULL COMMENT '更新人',
    `update_time`   datetime    NOT NULL COMMENT '更新时间',
    `del_flag`      char(1)      DEFAULT NULL COMMENT '删除标志 0:正常,1:删除',
    PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='客商信息';

-- 银行卡信息
DROP TABLE IF EXISTS mall_bank_card;
CREATE TABLE `mall_bank_card`
(
    `bank_card_id`   bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `bank_card_no`   varchar(20)    NOT NULL COMMENT '银行卡号',
    `bank_card_name` varchar(50)    NOT NULL COMMENT '卡名称',
    `address`        varchar(255)   NOT NULL COMMENT '银行地址',
    `money`          decimal(12, 2) NOT NULL COMMENT '存款',
    `bind_stat`      char(1)        NOT NULL COMMENT '是否绑定 0否1是',
    `customer_id`    bigint(20) DEFAULT NULL COMMENT '客商编号',
    `create_by`      varchar(50)    NOT NULL COMMENT '创建人',
    `create_time`    datetime       NOT NULL COMMENT '创建时间',
    `update_by`      varchar(50)    NOT NULL COMMENT '更新人',
    `update_time`    datetime       NOT NULL COMMENT '更新时间',
    `del_flag`       char(1) DEFAULT '0' COMMENT '删除标志 0:正常,1:删除',
    PRIMARY KEY (`bank_card_id`),
    KEY              `bank_card_no` (`bank_card_no`),
    KEY              `bind_stat` (`bind_stat`),
    KEY              `customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='银行卡信息';