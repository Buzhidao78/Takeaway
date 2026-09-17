package com.bzy.takeaway.service;

import com.bzy.takeaway.dto.CartVO;

import java.util.List;

public interface CartService {

    List<CartVO> list(Long userId);

    void add(Long userId, Long storeId, Long dishId, int quantity);

    void updateQuantity(Long userId, Long cartId, int quantity);

    void remove(Long userId, Long cartId);

    void clearByStore(Long userId, Long storeId);
}
