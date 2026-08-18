package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_status_log")
public class OrderStatusLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Integer orderType;
    
    private Integer oldStatus;
    
    private Integer newStatus;
    
    private Long operatorId;
    
    private Integer operatorType;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
