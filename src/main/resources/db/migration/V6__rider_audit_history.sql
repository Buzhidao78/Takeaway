CREATE TABLE IF NOT EXISTS `rider_audit_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rider_id` BIGINT NOT NULL,
  `rider_name` VARCHAR(50) NOT NULL,
  `rider_phone` VARCHAR(20) NOT NULL,
  `rider_id_card` VARCHAR(20) DEFAULT NULL,
  `audit_status` INT NOT NULL,
  `reject_reason` VARCHAR(500) DEFAULT NULL,
  `audit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `auditor_id` BIGINT DEFAULT NULL,
  `auditor_name` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_audit_time` (`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
