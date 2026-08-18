CREATE TABLE IF NOT EXISTS `store_earnings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT NOT NULL COMMENT 'Store ID',
  `order_id` BIGINT DEFAULT NULL COMMENT 'Order ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Amount',
  `type` TINYINT NOT NULL COMMENT 'Type: 1-order income, 2-refund, 3-withdrawal',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Store earnings table';
