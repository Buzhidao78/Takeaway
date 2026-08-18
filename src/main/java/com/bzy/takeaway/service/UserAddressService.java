package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bzy.takeaway.entity.UserAddress;
import com.bzy.takeaway.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressMapper addressMapper;

    public List<UserAddress> list(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId).orderByDesc(UserAddress::getIsDefault).orderByDesc(UserAddress::getId));
    }

    @Transactional
    public UserAddress save(Long userId, UserAddress addr) {
        addr.setUserId(userId);
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>().eq(UserAddress::getUserId, userId).set(UserAddress::getIsDefault, 0));
        }
        if (addr.getId() == null) {
            addressMapper.insert(addr);
        } else {
            addressMapper.updateById(addr);
        }
        return addr;
    }

    public void delete(Long userId, Long id) {
        addressMapper.delete(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getId, id).eq(UserAddress::getUserId, userId));
    }
}
