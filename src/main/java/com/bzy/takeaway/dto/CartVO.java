package com.bzy.takeaway.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long storeId;
    private Long dishId;
    private Integer quantity;
    private String dishName;
    private BigDecimal price;
    private String dishImage;
}
