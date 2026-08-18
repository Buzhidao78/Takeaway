-- 骑手接单系统数据库迁移脚本

-- 1. 修改 delivery_order 表，增加抢单相关字段
ALTER TABLE delivery_order 
ADD COLUMN grab_status INT DEFAULT 0 COMMENT '抢单状态: 0-待抢单 1-已接单 2-已取消',
ADD COLUMN grab_time DATETIME COMMENT '抢单时间',
ADD COLUMN expire_time DATETIME COMMENT '抢单过期时间';

-- 2. 新增 rider_online 表（骑手在线状态）
CREATE TABLE IF NOT EXISTS rider_online (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rider_id BIGINT NOT NULL COMMENT '骑手 ID',
    is_online TINYINT DEFAULT 0 COMMENT '是否在线: 0-离线 1-在线',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rider (rider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骑手在线状态表';

-- 3. 新增 order_status_log 表（订单状态变更日志）
CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    order_type TINYINT COMMENT '订单类型: 1-主订单 2-配送单',
    old_status INT COMMENT '原状态',
    new_status INT COMMENT '新状态',
    operator_id BIGINT COMMENT '操作人 ID',
    operator_type TINYINT COMMENT '操作人类型: 1-用户 2-商家 3-骑手 4-系统',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';
