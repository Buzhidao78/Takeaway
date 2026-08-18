CREATE TABLE IF NOT EXISTS `global_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `icon` VARCHAR(50) DEFAULT NULL,
  `sort` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `store_category_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_category` (`store_id`, `category_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `global_category` (`name`, `icon`, `sort`, `status`) VALUES
('美食', 'Food', 1, 1),
('超市', 'ShoppingCart', 2, 1),
('水果', 'Apple', 3, 1),
('药品', 'FirstAidKit', 4, 1),
('鲜花', 'Present', 5, 1),
('蛋糕', 'Cake', 6, 1),
('饮品', 'CoffeeCup', 7, 1);
