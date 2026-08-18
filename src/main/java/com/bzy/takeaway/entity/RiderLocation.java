package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rider_location")
public class RiderLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long riderId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
