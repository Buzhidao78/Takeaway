-- ========================================
-- 配送费系统数据库迁移脚本
-- 创建时间：2026-04-30
-- 功能：配送配置、配送区域、地址优化、订单优化
-- ========================================

-- 1. 商家配送配置表
CREATE TABLE IF NOT EXISTS `merchant_delivery_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家 ID',
  `base_fee` DECIMAL(10,2) DEFAULT 5.00 COMMENT '基础配送费',
  `free_delivery_threshold` DECIMAL(10,2) DEFAULT 50.00 COMMENT '满免配送费门槛',
  `min_order_amount` DECIMAL(10,2) DEFAULT 20.00 COMMENT '最低起送金额',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用配送：1=启用，0=禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant` (`merchant_id`) COMMENT '一个商家只能有一条配置',
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家配送配置表';

-- 2. 配送区域表
CREATE TABLE IF NOT EXISTS `delivery_zone` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家 ID',
  `province` VARCHAR(20) NOT NULL COMMENT '省',
  `city` VARCHAR(20) NOT NULL COMMENT '市',
  `district` VARCHAR(20) NOT NULL COMMENT '区/县',
  `zone_label` VARCHAR(50) COMMENT '区域标签（可选，如：校内、校外）',
  `additional_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '附加费',
  `min_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '该区域最低订单金额',
  `is_available` TINYINT DEFAULT 1 COMMENT '是否可配送：1=可配送，0=不可配送',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_area` (`province`, `city`, `district`),
  KEY `idx_available` (`is_available`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送区域表';

-- 3. 用户地址表优化（添加字段）
ALTER TABLE `user_address` 
ADD COLUMN `contact_name` VARCHAR(50) COMMENT '联系人姓名' AFTER `user_id`,
ADD COLUMN `contact_phone` VARCHAR(20) COMMENT '联系电话' AFTER `contact_name`,
ADD COLUMN `province` VARCHAR(20) COMMENT '省' AFTER `contact_phone`,
ADD COLUMN `city` VARCHAR(20) COMMENT '市' AFTER `province`,
ADD COLUMN `district` VARCHAR(20) COMMENT '区/县' AFTER `city`,
ADD COLUMN `street` VARCHAR(100) COMMENT '街道/乡镇' AFTER `district`,
ADD COLUMN `zone_label` VARCHAR(50) COMMENT '区域标签' AFTER `street`,
ADD COLUMN `address_type` TINYINT DEFAULT 1 COMMENT '地址类型：1=家，2=公司，3=学校，4=其他' AFTER `zone_label`,
ADD COLUMN `full_address` VARCHAR(300) COMMENT '完整地址' AFTER `address_type`,
ADD COLUMN `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址' AFTER `full_address`;

-- 添加索引
ALTER TABLE `user_address`
ADD KEY `idx_default` (`user_id`, `is_default`),
ADD KEY `idx_area` (`province`, `city`, `district`);

-- 4. 订单表优化（添加配送费、包装费字段）
ALTER TABLE `orders`
ADD COLUMN `packaging_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '包装费' AFTER `total_amount`,
ADD COLUMN `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费' AFTER `packaging_fee`,
ADD COLUMN `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额' AFTER `delivery_fee`;

-- 添加注释
ALTER TABLE `orders` 
MODIFY COLUMN `total_amount` DECIMAL(10,2) COMMENT '订单总金额（含配送费、包装费，减优惠）';

-- 5. 插入默认数据（可选）
-- 为现有商家插入默认配送配置（如果需要有默认数据的话）
-- INSERT INTO merchant_delivery_config (merchant_id, base_fee, free_delivery_threshold, min_order_amount, is_enabled)
-- SELECT id, 5.00, 50.00, 20.00, 1 FROM store WHERE deleted = 0;
