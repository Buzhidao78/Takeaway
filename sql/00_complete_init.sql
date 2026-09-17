-- ============================================================
-- BZY 外卖系统 数据库完整初始化脚本（离线部署专用）
-- 数据库: bzy_takeaway
--
-- 【说明】
--    测试账号（密码均为 123456，BCrypt 加密）：
--    管理员 13800000000 | 商家 13900000000 / 13900000001 / 13900000002
--    用户   13600000000 / 13600000001 / 13600000002
--    骑手   13700000000 / 13700000001
--    图片说明：dish.image / banner.image 使用 /frontend/images/xxx.jpg
--    相对路径，前端通过 /api/frontend/images/** 访问，实际文件需放在
--    服务器的 uploads/frontend/images/ 目录下（缺失时图片 404，不影响业务）。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 表结构
-- ----------------------------

-- ----------------------------
-- Table structure for banner 首页轮播
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
-- Table structure for cart 购物车
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
-- Table structure for category 店铺菜品分类
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
-- Table structure for delivery_order 配送单
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
-- Table structure for delivery_zone 配送区域
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
-- Table structure for dish 菜品
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
-- Table structure for global_category 全局分类
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
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for merchant_delivery_config 商家配送配置
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
-- Table structure for notification 通知
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
-- Table structure for order_item 订单明细
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
-- Table structure for order_status_log 订单状态日志
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
-- Table structure for orders 主订单
-- 金额关系：total_amount = goods_amount + packaging_fee + delivery_fee - discount_amount
--           pay_amount = total_amount
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
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额（含配送费、包装费，减优惠）',
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
-- Table structure for product_review 菜品评价
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
-- Table structure for review 店铺评价
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
-- Table structure for review_vote 评价投票
-- 注：review_id 关联 product_review.id（菜品评价），用户对同一评价只能投一票
-- ----------------------------
DROP TABLE IF EXISTS `review_vote`;
CREATE TABLE `review_vote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `review_id` bigint(20) NOT NULL COMMENT '评论 ID（关联 product_review.id）',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `vote_type` int(11) NOT NULL COMMENT '投票类型：1=点赞，-1=点踩',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`,`user_id`),
  KEY `idx_review_id` (`review_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for rider 骑手
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
-- Table structure for rider_audit_history 骑手审核记录
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
-- Table structure for rider_earnings 骑手收益
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
-- Table structure for rider_location 骑手位置
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
-- Table structure for rider_online 骑手在线状态
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
  UNIQUE KEY `uk_rider` (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for sms_log 短信验证码日志
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
-- Table structure for store 店铺
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
  `sales_count` int(11) DEFAULT '0' COMMENT '销量（= 全部在售菜品 sales 合计）',
  `rating` decimal(3,1) DEFAULT '5.0',
  `deleted` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `review_count` int(11) DEFAULT '0' COMMENT '评论数量',
  `carousel_images` text COLLATE utf8mb4_unicode_ci COMMENT '轮播图，逗号分隔',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '账户余额（= 订单收入 - 提现）',
  `total_orders` int(11) DEFAULT '0' COMMENT '累计有效订单数（不含已取消）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ----------------------------
-- Table structure for store_category_rel 店铺-全局分类关联
-- ----------------------------
DROP TABLE IF EXISTS `store_category_rel`;
CREATE TABLE `store_category_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `store_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_category` (`store_id`,`category_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for store_earnings 商家收益
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
-- Table structure for sys_user 系统用户
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
-- Table structure for user_address 收货地址
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
  KEY `idx_user_id` (`user_id`),
  KEY `idx_default` (`user_id`,`is_default`),
  KEY `idx_area` (`province`,`city`,`district`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 2. 种子数据（业务必需）
-- ============================================================

-- 全局分类（首页分类导航，前端强依赖，必须初始化）
INSERT INTO `global_category` (`id`, `name`, `icon`, `sort`, `status`, `deleted`) VALUES
(1, '美食',   'Food',          1, 1, 0),
(2, '超市',   'ShoppingCart',  2, 1, 0),
(3, '水果',   'Apple',         3, 1, 0),
(4, '药品',   'FirstAidKit',   4, 1, 0),
(5, '鲜花',   'Present',       5, 1, 0),
(6, '蛋糕',   'IceCream',      6, 1, 0),
(7, '饮品',   'Coffee',        7, 1, 0);

-- ============================================================
-- 3. 测试数据
--    密码统一为 123456（BCrypt 加密值）
--    $2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG
-- ============================================================

-- ----------------------------
-- 3.1 系统用户（管理员 1 / 商家 3 / 用户 3 / 骑手 2）
-- ----------------------------
INSERT INTO `sys_user` (`id`, `phone`, `password`, `nickname`, `role`, `status`, `create_time`, `update_time`) VALUES
(1, '13800000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '超级管理员', 2, 1, '2026-05-01 10:00:00', '2026-05-01 10:00:00'),
(2, '13900000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '张老板',     1, 1, '2026-05-02 09:00:00', '2026-05-02 09:00:00'),
(3, '13900000001', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '李老板',     1, 1, '2026-05-03 09:00:00', '2026-05-03 09:00:00'),
(4, '13900000002', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '王老板',     1, 1, '2026-05-04 09:00:00', '2026-05-04 09:00:00'),
(5, '13600000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '小明',       0, 1, '2026-05-10 10:00:00', '2026-05-10 10:00:00'),
(6, '13600000001', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '小红',       0, 1, '2026-05-11 10:00:00', '2026-05-11 10:00:00'),
(7, '13600000002', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '小刚',       0, 1, '2026-05-12 10:00:00', '2026-05-12 10:00:00'),
(8, '13700000000', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '小黄',       3, 1, '2026-06-01 09:00:00', '2026-06-01 09:00:00'),
(9, '13700000001', '$2a$10$c7i33JbLBk0BHp/DDmKWDuGIghvjeN/lQuYN31rAkHI85FC8B6dFG', '阿强',       3, 1, '2026-06-02 09:00:00', '2026-06-02 09:00:00');

-- ----------------------------
-- 3.2 骑手（余额 = 骑手收益订单收入合计，见 rider_earnings）
-- ----------------------------
INSERT INTO `rider` (`id`, `user_id`, `name`, `phone`, `id_card`, `status`, `audit_status`, `balance`, `total_orders`, `rating`, `create_time`) VALUES
(1, 8, '小黄', '13700000000', '110101199501011234', 1, 1, 11.00, 1, 4.90, '2026-06-01 09:10:00'),
(2, 9, '阿强', '13700000001', '110101199602021234', 1, 1, 11.00, 2, 4.80, '2026-06-02 09:10:00');

-- 骑手审核记录
INSERT INTO `rider_audit_history` (`id`, `rider_id`, `rider_name`, `rider_phone`, `rider_id_card`, `audit_status`, `reject_reason`, `audit_time`, `auditor_id`, `auditor_name`) VALUES
(1, 1, '小黄', '13700000000', '110101199501011234', 1, NULL, '2026-06-01 09:20:00', 1, '超级管理员'),
(2, 2, '阿强', '13700000001', '110101199602021234', 1, NULL, '2026-06-02 09:20:00', 1, '超级管理员');

-- 骑手在线状态 / 位置
INSERT INTO `rider_online` (`id`, `rider_id`, `is_online`, `last_heartbeat`) VALUES
(1, 1, 1, '2026-09-17 11:30:00'),
(2, 2, 0, '2026-09-16 20:00:00');

INSERT INTO `rider_location` (`id`, `rider_id`, `latitude`, `longitude`, `address`, `update_time`) VALUES
(1, 1, 39.984100, 116.491200, '北京市朝阳区望京街道', '2026-09-17 11:30:00');

-- ----------------------------
-- 3.3 店铺（3 家，logo/banner/carousel 置 NULL 避免无效图片）
--     sales_count = 该店全部菜品 sales 合计；balance = 订单收入 - 提现
-- ----------------------------
INSERT INTO `store` (`id`, `user_id`, `name`, `logo`, `banner`, `description`, `phone`, `address`, `longitude`, `latitude`, `open_time`, `status`, `audit_status`, `sales_count`, `rating`, `review_count`, `carousel_images`, `balance`, `total_orders`, `create_time`) VALUES
(1, 2, '川香居·川菜小馆', NULL, NULL, '地道川味，麻辣鲜香', '13900000000', '北京市朝阳区建国路88号', 116.473100, 39.909700, '10:00-22:00', 1, 1, 851, 4.8, 1, NULL, 92.00, 3, '2026-05-02 10:00:00'),
(2, 3, '米香园·快餐简餐', NULL, NULL, '现炒现做，实惠管饱', '13900000001', '北京市朝阳区望京SOHO T2', 116.482600, 39.996200, '09:30-21:30', 1, 1, 435, 4.6, 1, NULL, 43.00, 2, '2026-05-03 10:00:00'),
(3, 4, '鲜果汇·每日水果', NULL, NULL, '产地直采，新鲜到家', '13900000002', '北京市海淀区中关村大街27号', 116.316800, 39.982900, '08:30-22:30', 1, 1, 666, 4.9, 1, NULL, 67.00, 2, '2026-05-04 10:00:00');

-- 店铺-全局分类关联
INSERT INTO `store_category_rel` (`id`, `store_id`, `category_id`) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 3);

-- ----------------------------
-- 3.4 店铺菜品分类
-- ----------------------------
INSERT INTO `category` (`id`, `store_id`, `name`, `sort`, `create_time`) VALUES
(1,  1, '热销推荐', 1, '2026-05-02 10:00:00'),
(2,  1, '招牌菜',   2, '2026-05-02 10:00:00'),
(3,  1, '主食',     3, '2026-05-02 10:00:00'),
(4,  1, '饮品',     4, '2026-05-02 10:00:00'),
(5,  2, '热销推荐', 1, '2026-05-03 10:00:00'),
(6,  2, '米饭套餐', 2, '2026-05-03 10:00:00'),
(7,  2, '面条',     3, '2026-05-03 10:00:00'),
(8,  2, '饮品',     4, '2026-05-03 10:00:00'),
(9,  3, '时令水果', 1, '2026-05-04 10:00:00'),
(10, 3, '进口水果', 2, '2026-05-04 10:00:00'),
(11, 3, '果切',     3, '2026-05-04 10:00:00');

-- ----------------------------
-- 3.5 菜品（24 个，图片路径需配 uploads/frontend/images/ 目录文件）
-- ----------------------------
INSERT INTO `dish` (`id`, `store_id`, `category_id`, `name`, `image`, `description`, `price`, `origin_price`, `stock`, `sales`, `status`, `sort`, `create_time`) VALUES
-- 川香居
(1,  1, 1, '招牌红烧肉', '/frontend/images/dish_1.jpg',  '肥而不腻，入口即化',     28.00, 32.00, 200, 120, 1, 1, '2026-05-02 10:00:00'),
(2,  1, 1, '宫保鸡丁',   '/frontend/images/dish_2.jpg',  '经典川菜，酸甜微辣',     22.00, 25.00, 200, 98,  1, 2, '2026-05-02 10:00:00'),
(3,  1, 2, '水煮鱼',     '/frontend/images/dish_3.jpg',  '麻辣鲜嫩，份量十足',     45.00, 52.00, 100, 76,  1, 1, '2026-05-02 10:00:00'),
(4,  1, 2, '麻婆豆腐',   '/frontend/images/dish_4.jpg',  '麻辣下饭，豆腐嫩滑',     16.00, 18.00, 200, 88,  1, 2, '2026-05-02 10:00:00'),
(5,  1, 3, '扬州炒饭',   '/frontend/images/dish_5.jpg',  '粒粒分明，配料丰富',     18.00, 20.00, 300, 65,  1, 1, '2026-05-02 10:00:00'),
(6,  1, 3, '牛肉面',     '/frontend/images/dish_6.jpg',  '汤浓面劲，大块牛肉',     25.00, 28.00, 200, 54,  1, 2, '2026-05-02 10:00:00'),
(7,  1, 4, '珍珠奶茶',   '/frontend/images/dish_7.jpg',  '香浓丝滑，现制现售',     12.00, 15.00, 500, 200, 1, 1, '2026-05-02 10:00:00'),
(8,  1, 4, '酸梅汤',     '/frontend/images/dish_8.jpg',  '古法熬制，生津解腻',     8.00,  10.00, 500, 150, 1, 2, '2026-05-02 10:00:00'),
-- 米香园
(9,  2, 5, '招牌黄焖鸡米饭', '/frontend/images/dish_9.jpg',  '砂锅现焖，浓郁多汁', 20.00, 23.00, 300, 132, 1, 1, '2026-05-03 10:00:00'),
(10, 2, 6, '台式卤肉饭',     '/frontend/images/dish_10.jpg', '卤香四溢，肥瘦相间', 18.00, 20.00, 300, 87,  1, 1, '2026-05-03 10:00:00'),
(11, 2, 6, '农家小炒肉盖饭', '/frontend/images/dish_11.jpg', '现炒小炒肉，锅气十足', 22.00, 25.00, 200, 45,  1, 2, '2026-05-03 10:00:00'),
(12, 2, 6, '鱼香肉丝饭',     '/frontend/images/dish_12.jpg', '酸甜微辣，下饭神器', 19.00, 22.00, 200, 52,  1, 3, '2026-05-03 10:00:00'),
(13, 2, 7, '红烧牛肉面',     '/frontend/images/dish_13.jpg', '牛腩软烂，汤底浓郁', 26.00, 30.00, 200, 25,  1, 1, '2026-05-03 10:00:00'),
(14, 2, 7, '武汉热干面',     '/frontend/images/dish_14.jpg', '酱香浓郁，劲道爽口', 15.00, 18.00, 200, 61,  1, 2, '2026-05-03 10:00:00'),
(15, 2, 8, '冰镇可乐',       '/frontend/images/dish_15.jpg', '冰爽解渴，套餐搭配', 3.00,  3.00,  999, 0,   1, 1, '2026-05-03 10:00:00'),
(16, 2, 8, '鲜榨橙汁',       '/frontend/images/dish_16.jpg', '鲜果现榨，维C满满', 12.00, 15.00, 200, 33,  1, 2, '2026-05-03 10:00:00'),
-- 鲜果汇
(17, 3, 9,  '海南香蕉',       '/frontend/images/dish_17.jpg', '甜糯软绵，产地直采', 6.00,  6.50,  500, 210, 1, 1, '2026-05-04 10:00:00'),
(18, 3, 9,  '红富士苹果',     '/frontend/images/dish_18.jpg', '脆甜多汁，个大饱满', 8.00,  9.00,  500, 150, 1, 2, '2026-05-04 10:00:00'),
(19, 3, 9,  '麒麟西瓜',       '/frontend/images/dish_19.jpg', '皮薄瓤甜，沙瓤爆汁', 15.00, 18.00, 200, 88,  1, 3, '2026-05-04 10:00:00'),
(20, 3, 10, '进口车厘子',     '/frontend/images/dish_20.jpg', 'JJ级大果，甜脆爆汁', 39.00, 49.00, 100, 66,  1, 1, '2026-05-04 10:00:00'),
(21, 3, 10, '泰国金枕榴莲',   '/frontend/images/dish_21.jpg', '果肉金黄，绵密香甜', 79.00, 99.00, 50,  12,  1, 2, '2026-05-04 10:00:00'),
(22, 3, 10, '阳光玫瑰葡萄',   '/frontend/images/dish_22.jpg', '无籽爆甜，果香浓郁', 28.00, 35.00, 100, 45,  1, 3, '2026-05-04 10:00:00'),
(23, 3, 11, '西瓜果切',       '/frontend/images/dish_23.jpg', '现切现卖，冰镇更爽', 12.00, 15.00, 200, 40,  1, 1, '2026-05-04 10:00:00'),
(24, 3, 11, '混合水果拼盘',   '/frontend/images/dish_24.jpg', '多种鲜果，营养均衡', 18.00, 22.00, 200, 55,  1, 2, '2026-05-04 10:00:00');

-- ----------------------------
-- 3.6 首页轮播 Banner
-- ----------------------------
INSERT INTO `banner` (`id`, `title`, `subtitle`, `image`, `link`, `sort`, `status`, `create_time`) VALUES
(1, '新店开业 满30减10', '川香居 · 地道川味',   '/frontend/images/banner1.jpg', NULL, 1, 1, '2026-05-02 10:00:00'),
(2, '鲜果直采 新鲜到家', '鲜果汇 · 产地直供',   '/frontend/images/banner2.jpg', NULL, 2, 1, '2026-05-04 10:00:00'),
(3, '骑手招募 时间自由', '加入BZY 月入过万',   '/frontend/images/banner3.jpg', NULL, 3, 1, '2026-06-01 10:00:00');

-- ----------------------------
-- 3.7 用户收货地址
-- ----------------------------
INSERT INTO `user_address` (`id`, `user_id`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `street`, `zone_label`, `address_type`, `full_address`, `is_default`, `create_time`) VALUES
(1, 5, '张小明', '13600000000', '北京市', '北京市', '朝阳区', '建国路88号', '商圈核心', 1, '北京市朝阳区建国路88号2号楼302室', 1, '2026-05-10 10:00:00'),
(2, 5, '张小明', '13600000000', '北京市', '北京市', '朝阳区', '望京SOHO T1', '商圈核心', 2, '北京市朝阳区望京SOHO T1 12层', 0, '2026-05-11 10:00:00'),
(3, 6, '李小红', '13600000001', '北京市', '北京市', '朝阳区', '望京西园四区', '商圈核心', 1, '北京市朝阳区望京西园四区421号楼', 1, '2026-05-11 10:00:00'),
(4, 7, '王小刚', '13600000002', '北京市', '北京市', '海淀区', '中关村大街27号', '高校周边', 3, '北京市海淀区中关村大街27号学生公寓', 1, '2026-05-12 10:00:00');

-- ----------------------------
-- 3.8 商家配送配置 & 配送区域
-- ----------------------------
INSERT INTO `merchant_delivery_config` (`id`, `merchant_id`, `base_fee`, `free_delivery_threshold`, `min_order_amount`, `is_enabled`, `create_time`) VALUES
(1, 1, 5.00, 50.00, 20.00, 1, '2026-05-02 10:00:00'),
(2, 2, 6.00, 35.00, 15.00, 1, '2026-05-03 10:00:00'),
(3, 3, 5.00, 30.00, 20.00, 1, '2026-05-04 10:00:00');

INSERT INTO `delivery_zone` (`id`, `merchant_id`, `province`, `city`, `district`, `zone_label`, `additional_fee`, `min_order_amount`, `is_available`, `sort_order`, `create_time`) VALUES
(1, 1, '北京市', '北京市', '朝阳区', '商圈核心', 0.00, 20.00, 1, 1, '2026-05-02 10:00:00'),
(2, 1, '北京市', '北京市', '通州区', '远郊区域', 2.00, 30.00, 1, 2, '2026-05-02 10:00:00'),
(3, 2, '北京市', '北京市', '朝阳区', '商圈核心', 0.00, 15.00, 1, 1, '2026-05-03 10:00:00'),
(4, 3, '北京市', '北京市', '海淀区', '高校周边', 0.00, 20.00, 1, 1, '2026-05-04 10:00:00');

-- ============================================================
-- 3.9 订单（覆盖 0待支付 1已支付 2制作中 3配送中 4已完成 5已取消）
-- 金额勾稽：total = goods + packaging + delivery - discount；pay = total
-- ============================================================
INSERT INTO `orders`
(`id`, `order_no`, `user_id`, `store_id`, `address_id`,
 `goods_amount`, `packaging_fee`, `delivery_fee`, `total_amount`, `discount_amount`, `pay_amount`,
 `status`, `pay_type`, `pay_time`, `trade_no`, `remark`, `create_time`, `update_time`) VALUES
-- 订单1：已完成（满50免配送费）90 + 1 + 0 = 91
(1, 'BY202609100001', 5, 1, 1, 90.00, 1.00, 0.00, 91.00, 0.00, 91.00,
 4, 'alipay', '2026-09-10 10:02:00', '2026091010021000041234567890', '少放辣椒', '2026-09-10 10:00:00', '2026-09-10 10:40:00'),
-- 订单2：待支付（未满50收配送费）24 + 1 + 5 = 30
(2, 'BY202609170001', 5, 1, 1, 24.00, 1.00, 5.00, 30.00, 0.00, 30.00,
 0, NULL, NULL, NULL, NULL, '2026-09-17 09:30:00', '2026-09-17 09:30:00'),
-- 订单3：已支付（满50免配送费）50 + 1 + 0 = 51
(3, 'BY202609160001', 5, 1, 1, 50.00, 1.00, 0.00, 51.00, 0.00, 51.00,
 1, 'alipay', '2026-09-16 18:02:00', '2026091618021000041234567890', NULL, '2026-09-16 18:00:00', '2026-09-16 18:02:00'),
-- 订单4：已取消（未支付，用户取消）22 + 1 + 5 = 28
(4, 'BY202609140001', 5, 1, 1, 22.00, 1.00, 5.00, 28.00, 0.00, 28.00,
 5, NULL, NULL, NULL, '用户主动取消', '2026-09-14 12:00:00', '2026-09-14 12:10:00'),
-- 订单5：配送中（未满35收配送费）23 + 1 + 6 = 30
(5, 'BY202609170002', 6, 2, 3, 23.00, 1.00, 6.00, 30.00, 0.00, 30.00,
 3, 'alipay', '2026-09-17 11:02:00', '2026091711021000041234567890', '多加米饭', '2026-09-17 11:00:00', '2026-09-17 11:20:00'),
-- 订单6：已完成（满35免配送费）52 + 1 + 0 = 53
(6, 'BY202609120001', 6, 2, 3, 52.00, 1.00, 0.00, 53.00, 0.00, 53.00,
 4, 'alipay', '2026-09-12 12:02:00', '2026091212021000041234567890', NULL, '2026-09-12 12:00:00', '2026-09-12 12:45:00'),
-- 订单7：制作中（未满30收配送费）20 + 1.5 + 5 = 26.5
(7, 'BY202609170003', 7, 3, 4, 20.00, 1.50, 5.00, 26.50, 0.00, 26.50,
 2, 'alipay', '2026-09-17 10:52:00', '2026091710521000041234567890', '西瓜切块', '2026-09-17 10:50:00', '2026-09-17 10:55:00'),
-- 订单8：已完成（满30免配送费）39 + 1.5 + 0 = 40.5
(8, 'BY202609110001', 7, 3, 4, 39.00, 1.50, 0.00, 40.50, 0.00, 40.50,
 4, 'alipay', '2026-09-11 16:02:00', '2026091116021000041234567890', '要硬的不要软的', '2026-09-11 16:00:00', '2026-09-11 16:35:00');

-- ----------------------------
-- 3.10 订单明细（快照商品名称/价格）
-- ----------------------------
INSERT INTO `order_item` (`id`, `order_id`, `dish_id`, `dish_name`, `dish_image`, `price`, `quantity`, `amount`, `create_time`) VALUES
(1,  1, 1,  '招牌红烧肉',  '/frontend/images/dish_1.jpg',  28.00, 1, 28.00, '2026-09-10 10:00:00'),
(2,  1, 2,  '宫保鸡丁',    '/frontend/images/dish_2.jpg',  22.00, 2, 44.00, '2026-09-10 10:00:00'),
(3,  1, 5,  '扬州炒饭',    '/frontend/images/dish_5.jpg',  18.00, 1, 18.00, '2026-09-10 10:00:00'),
(4,  2, 7,  '珍珠奶茶',    '/frontend/images/dish_7.jpg',  12.00, 2, 24.00, '2026-09-17 09:30:00'),
(5,  3, 6,  '牛肉面',      '/frontend/images/dish_6.jpg',  25.00, 2, 50.00, '2026-09-16 18:00:00'),
(6,  4, 2,  '宫保鸡丁',    '/frontend/images/dish_2.jpg',  22.00, 1, 22.00, '2026-09-14 12:00:00'),
(7,  5, 9,  '招牌黄焖鸡米饭', '/frontend/images/dish_9.jpg', 20.00, 1, 20.00, '2026-09-17 11:00:00'),
(8,  5, 15, '冰镇可乐',    '/frontend/images/dish_15.jpg', 3.00,  1, 3.00,  '2026-09-17 11:00:00'),
(9,  6, 13, '红烧牛肉面',  '/frontend/images/dish_13.jpg', 26.00, 2, 52.00, '2026-09-12 12:00:00'),
(10, 7, 17, '海南香蕉',    '/frontend/images/dish_17.jpg', 6.00,  2, 12.00, '2026-09-17 10:50:00'),
(11, 7, 18, '红富士苹果',  '/frontend/images/dish_18.jpg', 8.00,  1, 8.00,  '2026-09-17 10:50:00'),
(12, 8, 20, '进口车厘子',  '/frontend/images/dish_20.jpg', 39.00, 1, 39.00, '2026-09-11 16:00:00');

-- ----------------------------
-- 3.11 配送单（fee 与订单 delivery_fee 一致）
-- ----------------------------
INSERT INTO `delivery_order`
(`id`, `order_id`, `rider_id`, `status`, `pick_up_time`, `delivery_time`, `estimated_time`, `distance`, `fee`,
 `grab_status`, `grab_time`, `expire_time`, `create_time`, `update_time`) VALUES
-- 订单1 已完成配送
(1, 1, 1, 3, '2026-09-10 10:20:00', '2026-09-10 10:40:00', '2026-09-10 10:50:00', 2.50, 0.00,
 1, '2026-09-10 10:10:00', NULL, '2026-09-10 10:05:00', '2026-09-10 10:40:00'),
-- 订单5 配送中（骑手小黄已取餐）
(2, 5, 1, 2, '2026-09-17 11:20:00', NULL, '2026-09-17 11:50:00', 3.20, 6.00,
 1, '2026-09-17 11:08:00', NULL, '2026-09-17 11:05:00', '2026-09-17 11:20:00'),
-- 订单7 待接单（无人接单，待抢单）
(3, 7, NULL, 0, NULL, NULL, '2026-09-17 11:20:00', 1.80, 5.00,
 0, NULL, '2026-09-17 11:20:00', '2026-09-17 10:53:00', '2026-09-17 10:53:00'),
-- 订单6 已完成配送（骑手阿强）
(4, 6, 2, 3, '2026-09-12 12:20:00', '2026-09-12 12:45:00', '2026-09-12 12:50:00', 4.10, 0.00,
 1, '2026-09-12 12:10:00', NULL, '2026-09-12 12:05:00', '2026-09-12 12:45:00'),
-- 订单8 已完成配送（骑手阿强）
(5, 8, 2, 3, '2026-09-11 16:15:00', '2026-09-11 16:35:00', '2026-09-11 16:40:00', 2.20, 0.00,
 1, '2026-09-11 16:08:00', NULL, '2026-09-11 16:05:00', '2026-09-11 16:35:00');

-- ----------------------------
-- 3.12 订单状态日志（operator_type: 1用户 2商家 3骑手 4系统）
-- ----------------------------
INSERT INTO `order_status_log` (`id`, `order_id`, `order_type`, `old_status`, `new_status`, `operator_id`, `operator_type`, `remark`, `create_time`) VALUES
-- 主订单1 完整流转
(1,  1, 1, 0, 1, 5, 1, '支付宝支付成功', '2026-09-10 10:02:00'),
(2,  1, 1, 1, 2, 2, 2, '商家接单开始制作', '2026-09-10 10:05:00'),
(3,  1, 1, 2, 3, 1, 3, '骑手取餐完成', '2026-09-10 10:20:00'),
(4,  1, 1, 3, 4, 1, 3, '骑手确认送达', '2026-09-10 10:40:00'),
-- 主订单5（配送中）
(5,  5, 1, 0, 1, 6, 1, '支付宝支付成功', '2026-09-17 11:02:00'),
(6,  5, 1, 1, 2, 3, 2, '商家接单开始制作', '2026-09-17 11:05:00'),
(7,  5, 1, 2, 3, 1, 3, '骑手取餐完成', '2026-09-17 11:20:00'),
-- 主订单7（制作中）
(8,  7, 1, 0, 1, 7, 1, '支付宝支付成功', '2026-09-17 10:52:00'),
(9,  7, 1, 1, 2, 4, 2, '商家接单开始制作', '2026-09-17 10:55:00'),
-- 配送单1（已完成）
(10, 1, 2, 0, 1, 1, 3, '骑手抢单成功', '2026-09-10 10:10:00'),
(11, 1, 2, 1, 2, 1, 3, '骑手取餐', '2026-09-10 10:20:00'),
(12, 1, 2, 2, 3, 1, 3, '骑手送达', '2026-09-10 10:40:00'),
-- 配送单2（配送中）
(13, 5, 2, 0, 1, 1, 3, '骑手抢单成功', '2026-09-17 11:08:00'),
(14, 5, 2, 1, 2, 1, 3, '骑手取餐', '2026-09-17 11:20:00'),
-- 配送单3（待接单，仅生成记录）
(15, 7, 2, 0, 0, NULL, 4, '配送单生成，等待骑手抢单', '2026-09-17 10:53:00');

-- ----------------------------
-- 3.13 评价（店铺评价 + 菜品评价 + 投票）
-- ----------------------------
INSERT INTO `review` (`id`, `order_id`, `user_id`, `store_id`, `rating`, `content`, `reply_content`, `reply_time`, `status`, `create_time`) VALUES
(1, 1, 5, 1, 5, '红烧肉肥而不腻，配送也很快，下次还点！', '感谢您的认可，欢迎再次光临！', '2026-09-10 14:00:00', 1, '2026-09-10 12:00:00'),
(2, 6, 6, 2, 4, '黄焖鸡味道不错，就是等得稍久了一点。', NULL, NULL, 1, '2026-09-12 15:00:00'),
(3, 8, 7, 3, 5, '车厘子很新鲜很甜，果切份量也足，五星好评！', '感谢支持，鲜果汇持续为您提供新鲜水果！', '2026-09-11 18:00:00', 1, '2026-09-11 17:00:00');

INSERT INTO `product_review`
(`id`, `order_item_id`, `order_id`, `user_id`, `store_id`, `dish_id`, `rating`, `content`, `like_count`, `dislike_count`, `reply_content`, `reply_time`, `status`, `create_time`) VALUES
(1, 1, 1, 5, 1, 1, 5, '红烧肉入口即化，强烈推荐！', 2, 0, '感谢好评！', '2026-09-10 14:00:00', 1, '2026-09-10 12:00:00'),
(2, 2, 1, 5, 1, 2, 4, '宫保鸡丁挺下饭，花生米很脆。', 1, 0, NULL, NULL, 1, '2026-09-10 12:00:00'),
(3, 7, 5, 6, 2, 9, 4, '黄焖鸡米饭汤汁浓郁，味道不错。', 0, 0, NULL, NULL, 1, '2026-09-12 15:00:00'),
(4, 12, 8, 7, 3, 20, 5, '车厘子个大味甜，物流保鲜很好。', 0, 0, NULL, NULL, 1, '2026-09-11 17:00:00');

-- 投票记录（与 product_review.like_count 严格对应：评论1 两票，评论2 一票）
INSERT INTO `review_vote` (`id`, `review_id`, `user_id`, `vote_type`, `create_time`) VALUES
(1, 1, 6, 1, '2026-09-10 13:00:00'),
(2, 1, 7, 1, '2026-09-10 13:30:00'),
(3, 2, 7, 1, '2026-09-10 14:00:00');

-- ----------------------------
-- 3.14 收益流水（商家余额 = 收入合计 - 提现；骑手余额 = 收入合计）
-- ----------------------------
INSERT INTO `store_earnings` (`id`, `store_id`, `order_id`, `amount`, `type`, `created_at`) VALUES
(1, 1, 1, 91.00, 1, '2026-09-10 10:40:00'),   -- 川香居 订单1 收入
(2, 1, 3, 51.00, 1, '2026-09-16 18:02:00'),   -- 川香居 订单3 收入
(3, 1, NULL, 50.00, 3, '2026-09-15 09:00:00'), -- 川香居 提现（余额 91+51-50=92）
(4, 2, 5, 30.00, 1, '2026-09-17 11:20:00'),   -- 米香园 订单5 收入
(5, 2, 6, 53.00, 1, '2026-09-12 12:45:00'),   -- 米香园 订单6 收入
(6, 2, NULL, 40.00, 3, '2026-09-14 09:00:00'), -- 米香园 提现（余额 30+53-40=43）
(7, 3, 7, 26.50, 1, '2026-09-17 10:55:00'),   -- 鲜果汇 订单7 收入
(8, 3, 8, 40.50, 1, '2026-09-11 16:35:00');   -- 鲜果汇 订单8 收入（余额 26.5+40.5=67）

INSERT INTO `rider_earnings` (`id`, `rider_id`, `order_id`, `amount`, `type`, `create_time`) VALUES
(1, 1, 1, 5.00, 1, '2026-09-10 10:40:00'),   -- 小黄 配送单1（订单1）配送费
(2, 1, 5, 6.00, 1, '2026-09-17 11:20:00'),   -- 小黄 配送单2（订单5）配送费
(3, 2, 6, 6.00, 1, '2026-09-12 12:45:00'),   -- 阿强 配送单4（订单6）配送费
(4, 2, 8, 5.00, 1, '2026-09-11 16:35:00');   -- 阿强 配送单5（订单8）配送费

-- ----------------------------
-- 3.15 购物车
-- ----------------------------
INSERT INTO `cart` (`id`, `user_id`, `store_id`, `dish_id`, `quantity`, `create_time`) VALUES
(1, 5, 1, 1, 1, '2026-09-17 09:20:00'),
(2, 5, 1, 7, 1, '2026-09-17 09:20:00'),
(3, 6, 2, 15, 2, '2026-09-17 11:00:00');

-- ----------------------------
-- 3.16 通知
-- ----------------------------
INSERT INTO `notification` (`id`, `user_id`, `title`, `content`, `type`, `related_id`, `is_read`, `audit_status`, `create_time`) VALUES
(1, 5, '订单已送达', '您的订单 BY202609100001 已送达，祝您用餐愉快！', 'order', 1, 1, 1, '2026-09-10 10:40:00'),
(2, 5, '欢迎使用BZY外卖', '注册即送新人礼包，首单立减！', 'system', NULL, 0, 1, '2026-05-10 10:00:00'),
(3, 6, '订单配送中', '您的订单 BY202609170002 骑手正在配送中，请留意接听电话。', 'order', 5, 0, 1, '2026-09-17 11:20:00'),
(4, 7, '订单已完成', '您的订单 BY202609110001 已完成，期待您的评价！', 'order', 8, 1, 1, '2026-09-11 16:35:00');

-- ============================================================
-- 4. 初始化完成校验（自查数据一致性）
-- ============================================================
-- 1) 全局分类：应返回 7 行
SELECT COUNT(*) AS global_category_count FROM `global_category`;
-- 2) 金额勾稽：应返回 0 行（total_amount 必须等于各项之和）
SELECT id, order_no FROM `orders`
WHERE total_amount <> goods_amount + IFNULL(packaging_fee,0) + IFNULL(delivery_fee,0) - IFNULL(discount_amount,0);
-- 3) 商家余额对账：以下余额应与 store.balance 一致
--    store1 = 91+51-50 = 92；store2 = 30+53-40 = 43；store3 = 26.5+40.5 = 67
-- 4) 骑手余额对账：rider1 = 5+6 = 11；rider2 = 6+5 = 11
