package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private Long addressId;
    
    /**
     * 商品总金额
     */
    private BigDecimal goodsAmount;
    
    /**
     * 包装费
     */
    private BigDecimal packagingFee;
    
    /**
     * 配送费
     */
    private BigDecimal deliveryFee;
    
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 订单总金额（含配送费、包装费，减优惠）
     */
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    private Integer status;
    private String payType;
    private LocalDateTime payTime;
    private String tradeNo;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<OrderItem> items;
}
