package com.bzy.takeaway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bzy.takeaway.entity.ProductReview;
import com.bzy.takeaway.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
    
    @Select("SELECT * FROM sys_user WHERE id = #{userId}")
    SysUser getSysUserById(Long userId);
}
