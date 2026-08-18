package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rider")
public class Rider {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String name;
    private String phone;
    private String idCard;
    private String avatar;
    
    private Integer status;
    
    @TableField("audit_status")
    private Integer auditStatus;
    
    private BigDecimal balance;
    private Integer totalOrders;
    private BigDecimal rating;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private String token;
    
    @TableField(exist = false)
    private String password;
}
