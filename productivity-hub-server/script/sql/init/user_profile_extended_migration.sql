-- 用户个人信息扩展字段迁移脚本
-- 为 sys_user 表添加更多个人信息字段

ALTER TABLE `sys_user`
ADD COLUMN `gender` varchar(10) DEFAULT NULL COMMENT '性别（男/女/其他）' AFTER `phone`,
ADD COLUMN `birthday` date DEFAULT NULL COMMENT '生日' AFTER `gender`,
ADD COLUMN `address` varchar(500) DEFAULT NULL COMMENT '地址' AFTER `birthday`,
ADD COLUMN `company` varchar(200) DEFAULT NULL COMMENT '公司/组织' AFTER `address`,
ADD COLUMN `position` varchar(100) DEFAULT NULL COMMENT '职位' AFTER `company`,
ADD COLUMN `website` varchar(500) DEFAULT NULL COMMENT '个人网站' AFTER `position`;

