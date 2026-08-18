package com.bzy.takeaway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bzy.takeaway.entity.Rider;
import org.apache.ibatis.annotations.Param;

public interface RiderMapper extends BaseMapper<Rider> {
    
    /**
     * 物理删除骑手（忽略逻辑删除）
     */
    int deletePhysical(@Param("id") Long id);
    
    /**
     * 根据手机号查询骑手（忽略逻辑删除）
     */
    Rider selectByPhoneIgnoreDeleted(@Param("phone") String phone);
    
    /**
     * 更新骑手手机号（忽略逻辑删除）
     */
    int updatePhoneIgnoreDeleted(@Param("id") Long id, @Param("phone") String phone);
}
