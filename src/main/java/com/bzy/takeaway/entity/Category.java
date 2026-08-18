package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private String name;
    private Integer sort;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}
