package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_delivery_config")
public class MerchantDeliveryConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;
    
    /**
     * 基础配送费
     */
    private BigDecimal baseFee;
    
    /**
     * 满免配送费门槛
     */
    private BigDecimal freeDeliveryThreshold;
    
    /**
     * 最低起送金额
     */
    private BigDecimal minOrderAmount;
    
    /**
     * 是否启用配送：1=启用，0=禁用
     */
    private Integer isEnabled;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
