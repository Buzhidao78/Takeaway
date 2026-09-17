package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Orders;

import java.math.BigDecimal;
import java.util.Map;

public interface OrderService {

    Orders create(Long userId, Long storeId, Long addressId, String remark, BigDecimal packagingFee, BigDecimal deliveryFee);

    Page<Orders> myOrders(Long userId, Integer status, int page, int size);

    Orders detail(Long userId, Long orderId);

    String createPayForm(Long userId, Long orderId);

    void handleAlipayNotify(Map<String, String> params);

    void paySuccess(Long userId, Long orderId);

    boolean queryPayStatus(Long userId, String orderNo);

    void cancel(Long userId, Long orderId);

    void refund(Long userId, Long orderId);
}