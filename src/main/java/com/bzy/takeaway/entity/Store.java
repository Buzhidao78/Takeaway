package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@TableName("store")
public class Store {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String phone;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String openTime;
    private Integer status;
    private Integer auditStatus;
    private String rejectReason;
    private Integer salesCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal balance;
    private Integer totalOrders;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String carouselImages;  // 数据库中存储逗号分隔的字符串
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 自定义 setter，接收数组并转换为逗号分隔字符串
    @com.fasterxml.jackson.annotation.JsonProperty("carouselImages")
    public void setCarouselImagesFromJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            this.carouselImages = null;
        } else {
            this.carouselImages = String.join(",", images);
        }
    }
    
    // getter 方法，将逗号分隔字符串转换为数组返回
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<String> getCarouselImagesAsList() {
        if (this.carouselImages == null || this.carouselImages.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(this.carouselImages.split(","));
    }
}
