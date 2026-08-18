package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_review")
public class ProductReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderItemId;
    private Long orderId;
    private Long userId;
    private Long storeId;
    private Long dishId;
    private Integer rating;
    private String content;
    private String images;
    private String additionalContent;  // 追评内容
    private String additionalImages;   // 追评图片
    private LocalDateTime additionalTime; // 追评时间
    private Integer likeCount;         // 点赞数
    private Integer dislikeCount;      // 点踩数
    private String replyContent;
    private LocalDateTime replyTime;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String dishName;
    
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String userAvatar;
}
