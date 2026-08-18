package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("delivery_zone")
public class DeliveryZone {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;
    
    /**
     * 省
     */
    private String province;
    
    /**
     * 市
     */
    private String city;
    
    /**
     * 区/县
     */
    private String district;
    
    /**
     * 区域标签（如：校内、校外）
     */
    private String zoneLabel;
    
    /**
     * 附加费
     */
    private BigDecimal additionalFee;
    
    /**
     * 该区域最低订单金额
     */
    private BigDecimal minOrderAmount;
    
    /**
     * 是否可配送：1=可配送，0=不可配送
     */
    private Integer isAvailable;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
