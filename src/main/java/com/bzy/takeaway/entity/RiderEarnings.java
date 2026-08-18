package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rider_earnings")
public class RiderEarnings {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long riderId;
    private Long orderId;
    private BigDecimal amount;
    private Integer type;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String orderNo;
}
