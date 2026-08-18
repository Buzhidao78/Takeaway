package com.bzy.takeaway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bzy.takeaway.entity.Cart;
import org.apache.ibatis.annotations.Param;

public interface CartMapper extends BaseMapper<Cart> {
    
    /**
     * 物理删除购物车记录（忽略软删除）
     */
    int deletePhysical(@Param("userId") Long userId, @Param("storeId") Long storeId, @Param("dishId") Long dishId);
}
