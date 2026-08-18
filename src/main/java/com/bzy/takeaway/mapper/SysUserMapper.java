package com.bzy.takeaway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bzy.takeaway.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 物理删除用户（忽略逻辑删除）
     */
    int deletePhysical(@Param("id") Long id);
    
    /**
     * 根据ID查询用户（忽略逻辑删除）
     */
    SysUser selectByIdIgnoreDeleted(@Param("id") Long id);
    
    /**
     * 更新用户（忽略逻辑删除）
     */
    int updateByIdIgnoreDeleted(@Param("user") SysUser user);
    
    /**
     * 原生 SQL 查询用户列表
     */
    @Select("${sql}")
    List<SysUser> selectListBySql(@Param("sql") String sql);
    
    /**
     * 原生 SQL 查询用户数量
     */
    @Select("${sql}")
    Long selectCountBySql(@Param("sql") String sql);
}
