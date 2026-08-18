package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("delivery_order")
public class DeliveryOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    private Long riderId;
    
    private Integer status;
    private LocalDateTime pickUpTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedTime;
    private BigDecimal distance;
    private BigDecimal fee;
    
    private Integer grabStatus;
    private LocalDateTime grabTime;
    private LocalDateTime expireTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private Rider rider;
    
    @TableField(exist = false)
    private Orders order;
}
