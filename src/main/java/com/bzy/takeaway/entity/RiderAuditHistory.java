package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rider_audit_history")
public class RiderAuditHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private String riderIdCard;
    
    private Integer auditStatus;
    private String rejectReason;
    
    private LocalDateTime auditTime;
    private Long auditorId;
    private String auditorName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
