package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private Long categoryId;
    private String name;
    private String image;
    private String images;
    private String description;
    private BigDecimal price;
    private BigDecimal originPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
    private Integer sort;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private Double rating;
    
    @TableField(exist = false)
    private Integer salesCount;
    
    @TableField(exist = false)
    private String storeName;
    
    public boolean validate() {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格必须大于 0");
        }
        if (originPrice != null && originPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("原价不能为负数");
        }
        if (originPrice != null && price.compareTo(originPrice) > 0) {
            throw new IllegalArgumentException("现价不能高于原价");
        }
        if (originPrice != null && price.compareTo(originPrice) >= 0) {
            System.out.println("警告：现价等于或接近原价，建议直接设置原价");
        }
        return true;
    }
}
