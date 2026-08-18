package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("store_earnings")
public class StoreEarnings {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long storeId;
    private Long orderId;
    private BigDecimal amount;
    private Integer type;
    
    @TableField(fill = FieldFill.INSERT, value = "created_at")
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String orderNo;
}
