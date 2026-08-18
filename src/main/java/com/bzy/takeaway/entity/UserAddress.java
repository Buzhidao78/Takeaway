package com.bzy.takeaway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_address")
public class UserAddress {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
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
     * 街道/乡镇
     */
    private String street;
    
    /**
     * 区域标签
     */
    private String zoneLabel;
    
    /**
     * 地址类型：1=家，2=公司，3=学校，4=其他
     */
    private Integer addressType;
    
    /**
     * 完整地址
     */
    private String fullAddress;
    
    /**
     * 是否默认地址
     */
    private Integer isDefault;
    
    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
