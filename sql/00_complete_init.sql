-- MySQL Database Initialization Script
-- Database: bzy_takeaway
-- Generated: 2026-05-08

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subtitle` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '副标题',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `store_id` bigint(20) NOT NULL,
  `dish_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '1',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store_dish` (`user_id`,`store_id`,`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `store_id` bigint(20) NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int(11) DEFAULT '0',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类';

-- ----------------------------
-- Table structure for delivery_order
-- ----------------------------
DROP TABLE IF EXISTS `delivery_order`;
CREATE TABLE `delivery_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配送单ID',
  `order_id` bigint(20) NOT NULL COMMENT '主订单ID',
  `rider_id` bigint(20) DEFAULT NULL COMMENT '骑手ID',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-待接单 1-已接单 2-配送中 3-已完成 4-已取消',
  `pick_up_time` datetime DEFAULT NULL COMMENT '取餐时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `estimated_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `distance` decimal(10,2) DEFAULT NULL COMMENT '配送距离(公里)',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '配送费',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `grab_status` int(11) DEFAULT '0' COMMENT '抢单状态：0-待抢单 1-已接单 2-已取消',
  `grab_time` datetime DEFAULT NULL COMMENT '抢单时间',
  `expire_time` datetime DEFAULT NULL COMMENT '抢单过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for delivery_zone
-- ----------------------------
DROP TABLE IF EXISTS `delivery_zone`;
CREATE TABLE `delivery_zone` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家 ID',
  `province` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省',
  `city` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市',
  `district` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县',
  `zone_label` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域标签',
  `additional_fee` decimal(10,2) DEFAULT '0.00' COMMENT '附加费',
  `min_order_amount` decimal(10,2) DEFAULT '0.00' COMMENT '该区域最低订单金额',
  `is_available` tinyint(4) DEFAULT '1' COMMENT '是否可配送：1=可配送，0=不可配送',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_area` (`province`,`city`,`district`),
  KEY `idx_available` (`is_available`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for dish
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `store_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `price` decimal(10,2) NOT NULL,
  `origin_price` decimal(10,2) DEFAULT NULL,
  `stock` int(11) DEFAULT '999',
  `sales` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT '0下架 1上架',
  `sort` int(11) DEFAULT '0',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for global_category
-- ----------------------------
DROP TABLE IF EXISTS `global_category`;
CREATE TABLE `global_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort` int(11) DEFAULT '0',
  `status` int(11) DEFAULT '1',
  `deleted` int(11) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for merchant_delivery_config
-- ----------------------------
DROP TABLE IF EXISTS `merchant_delivery_config`;
CREATE TABLE `merchant_delivery_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家 ID',
  `base_fee` decimal(10,2) DEFAULT '5.00' COMMENT '基础配送费',
  `free_delivery_threshold` decimal(10,2) DEFAULT '50.00' COMMENT '满免配送费门槛',
  `min_order_amount` decimal(10,2) DEFAULT '20.00' COMMENT '最低起送金额',
  `is_enabled` tinyint(4) DEFAULT '1' COMMENT '是否启用配送：1=启用，0=禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant` (`merchant_id`),
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '通知内容',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：order-订单, system-系统, promotion-促销',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID（订单ID等）',
  `is_read` tinyint(4) DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audit_status` int(11) DEFAULT '0' COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `dish_id` bigint(20) NOT NULL,
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dish_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for order_status_log
-- ----------------------------
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单 ID',
  `order_type` tinyint(4) DEFAULT NULL COMMENT '订单类型: 1-主订单 2-配送单',
  `old_status` int(11) DEFAULT NULL COMMENT '原状态',
  `new_status` int(11) DEFAULT NULL COMMENT '新状态',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人 ID',
  `operator_type` tinyint(4) DEFAULT NULL COMMENT '操作人类型：1-用户 2-商家 3-骑手 4-系统',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `store_id` bigint(20) NOT NULL,
  `address_id` bigint(20) NOT NULL,
  `goods_amount` decimal(10,2) DEFAULT NULL COMMENT '商品总金额',
  `packaging_fee` decimal(10,2) DEFAULT NULL COMMENT '包装费',
  `delivery_fee` decimal(10,2) DEFAULT NULL COMMENT '配送费',
  `total_amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT '0.00',
  `pay_amount` decimal(10,2) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0待支付 1已支付 2制作中 3配送中 4已完成 5已取消',
  `pay_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'alipay/wechat',
  `pay_time` datetime DEFAULT NULL,
  `trade_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付流水号',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for product_review
-- ----------------------------
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_item_id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `store_id` bigint(20) NOT NULL,
  `dish_id` bigint(20) NOT NULL,
  `rating` int(11) NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `additional_content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '追评内容',
  `additional_images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '追评图片',
  `additional_time` datetime DEFAULT NULL COMMENT '追评时间',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `dislike_count` int(11) DEFAULT '0' COMMENT '点踩数',
  `reply_content` text COLLATE utf8mb4_unicode_ci,
  `reply_time` datetime DEFAULT NULL,
  `status` int(11) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `store_id` bigint(20) NOT NULL COMMENT '店铺 ID',
  `rating` int(11) NOT NULL COMMENT '评分 1-5',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `reply_content` text COLLATE utf8mb4_unicode_ci COMMENT '商家回复',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-待审核 1-已发布 2-已屏蔽',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for review_vote
-- ----------------------------
DROP TABLE IF EXISTS `review_vote`;
CREATE TABLE `review_vote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `review_id` bigint(20) NOT NULL COMMENT '评论 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `vote_type` int(11) NOT NULL COMMENT '投票类型：1=点赞，-1=点踩',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider
-- ----------------------------
DROP TABLE IF EXISTS `rider`;
CREATE TABLE `rider` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联用户ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '骑手姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `id_card` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未激活 1-正常 2-禁用',
  `audit_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `total_orders` int(11) NOT NULL DEFAULT '0' COMMENT '总订单数',
  `rating` decimal(3,2) NOT NULL DEFAULT '5.00' COMMENT '评分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider_audit_history
-- ----------------------------
DROP TABLE IF EXISTS `rider_audit_history`;
CREATE TABLE `rider_audit_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rider_id` bigint(20) NOT NULL,
  `rider_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rider_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rider_id_card` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_status` int(11) NOT NULL,
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `auditor_id` bigint(20) DEFAULT NULL,
  `auditor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_audit_time` (`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider_earnings
-- ----------------------------
DROP TABLE IF EXISTS `rider_earnings`;
CREATE TABLE `rider_earnings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rider_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT '收益金额',
  `type` tinyint(4) NOT NULL COMMENT '收益类型：1-订单收入，2-提现，3-退款',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider_location
-- ----------------------------
DROP TABLE IF EXISTS `rider_location`;
CREATE TABLE `rider_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rider_id` bigint(20) NOT NULL,
  `latitude` decimal(10,6) NOT NULL,
  `longitude` decimal(10,6) NOT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider_online
-- ----------------------------
DROP TABLE IF EXISTS `rider_online`;
CREATE TABLE `rider_online` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rider_id` bigint(20) NOT NULL,
  `is_online` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否在线：0-离线 1-在线',
  `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for sms_log
-- ----------------------------
DROP TABLE IF EXISTS `sms_log`;
CREATE TABLE `sms_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：login-登录，register-注册',
  `expire_time` datetime NOT NULL,
  `used` tinyint(4) DEFAULT '0' COMMENT '是否已使用：0-未使用 1-已使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for store
-- ----------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '商家用户ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `logo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `banner` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `open_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '09:00-22:00',
  `status` tinyint(4) DEFAULT '1' COMMENT '0关闭 1营业 2休息',
  `audit_status` tinyint(4) DEFAULT '0' COMMENT '0待审核 1通过 2拒绝',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核拒绝理由',
  `sales_count` int(11) DEFAULT '0' COMMENT '销量',
  `rating` decimal(3,1) DEFAULT '5.0',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `review_count` int(11) DEFAULT '0' COMMENT '评论数量',
  `carousel_images` text COLLATE utf8mb4_unicode_ci COMMENT '轮播图，逗号分隔',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT 'Account balance',
  `total_orders` int(11) DEFAULT '0' COMMENT 'Total orders',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ----------------------------
-- Table structure for store_category_rel
-- ----------------------------
DROP TABLE IF EXISTS `store_category_rel`;
CREATE TABLE `store_category_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `store_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for store_earnings
-- ----------------------------
DROP TABLE IF EXISTS `store_earnings`;
CREATE TABLE `store_earnings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `store_id` bigint(20) NOT NULL COMMENT '商家 ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单 ID',
  `amount` decimal(10,2) NOT NULL COMMENT '收益金额',
  `type` tinyint(4) NOT NULL COMMENT '收益类型：1-订单收入，2-退款，3-提现',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` int(11) NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户 1-商家 2-管理员 3-骑手',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-禁用 1-正常',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `contact_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区',
  `street` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '街道',
  `zone_label` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域标签',
  `address_type` int(11) DEFAULT NULL COMMENT '地址类型：1=家，2=公司，3=学校，4=其他',
  `full_address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '完整地址',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '默认地址',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 测试数据
-- ============================================

-- 测试用户数据
INSERT INTO `sys_user` (`id`, `phone`, `password`, `nickname`, `role`, `status`) VALUES
(1, '13800000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '超级管理员', 2, 1),
(2, '13900000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '测试商家', 1, 1),
(60, '13600000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '测试用户', 0, 1),
(62, '13700000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '小黄', 0, 1);

-- 测试骑手数据
INSERT INTO `rider` (`id`, `user_id`, `name`, `phone`, `id_card`, `status`, `audit_status`, `balance`, `total_orders`, `rating`) VALUES
(1, 60, '测试骑手', '13600000000', '110101199001011234', 1, 1, 100.00, 50, 4.85);

-- 测试店铺数据
INSERT INTO `store` (`id`, `user_id`, `name`, `logo`, `banner`, `description`, `phone`, `address`, `longitude`, `latitude`, `open_time`, `status`, `audit_status`, `sales_count`, `rating`, `review_count`, `balance`, `total_orders`) VALUES
(1, 2, 'BZY 美味餐厅', NULL, NULL, '各种美食，应有尽有', '13900000000', '北京市朝阳区 xxx 街道', 116.404, 39.915, '09:00-22:00', 1, 1, 100, 4.8, 50, 1000.00, 100);

-- 用户和测试数据说明
-- 管理员：13800000000 / 123456
-- 商家：13900000000 / 123456（已创建测试店铺）
-- 用户：13600000000 / 123456
-- 骑手：13700000000 / 123456 (小黄)
