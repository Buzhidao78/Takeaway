package com.bzy.takeaway.service;

import com.bzy.takeaway.entity.UserAddress;

import java.util.List;

public interface UserAddressService {

    List<UserAddress> list(Long userId);

    UserAddress save(Long userId, UserAddress addr);

    void delete(Long userId, Long id);
}