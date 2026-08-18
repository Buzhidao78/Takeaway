ALTER TABLE `store` ADD COLUMN `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Account balance';
ALTER TABLE `store` ADD COLUMN `total_orders` INT DEFAULT 0 COMMENT 'Total orders';
