package com.bzy.takeaway.service;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.SysUser;

public interface AuthService {

    Result<SysUser> login(String phone, String password, String captcha);

    /**
     * 生成登录验证码
     */
    Result<String> generateLoginCaptcha(String phone);

    /**
     * 获取登录失败次数
     */
    Result<Integer> getLoginFailureCount(String phone);

    SysUser getUserById(Long userId);

    /**
     * 重置密码
     */
    Result<String> resetPassword(String phone, String password, String code);

    Result<String> sendCode(String phone, String type);

    Result<SysUser> register(String phone, String password, String code, String nickname);

    Result<SysUser> merchantRegister(String phone, String password, String code,
                                     String storeName, String storeAddress, String storePhone);
}
