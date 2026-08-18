package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rider_online")
public class RiderOnline {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long riderId;
    
    private Integer isOnline;
    
    private LocalDateTime lastHeartbeat;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
