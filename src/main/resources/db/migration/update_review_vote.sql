-- 清空点赞点踩数据
UPDATE product_review SET like_count = 0, dislike_count = 0 WHERE deleted = 0;

-- 创建用户投票记录表
CREATE TABLE IF NOT EXISTS review_vote (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
  review_id BIGINT NOT NULL COMMENT '评论 ID',
  user_id BIGINT NOT NULL COMMENT '用户 ID',
  vote_type INT NOT NULL COMMENT '投票类型：1=点赞，-1=点踩',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_review_user (review_id, user_id) COMMENT '用户对同一评论只能投一次票',
  KEY idx_review_id (review_id) COMMENT '评论 ID 索引',
  KEY idx_user_id (user_id) COMMENT '用户 ID 索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论投票记录表';
