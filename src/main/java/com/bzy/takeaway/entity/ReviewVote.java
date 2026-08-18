package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_vote")
public class ReviewVote {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reviewId;
    private Long userId;
    private Integer voteType;  // 1=点赞，-1=点踩
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
