package com.bzy.takeaway.service;

/**
 * 短信验证码服务
 * 开发模式：验证码输出到日志，支持任意6位验证（便于测试）
 * 生产模式：接入阿里云短信
 */
public interface SmsService {

    /**
     * 发送验证码
     */
    void sendCode(String phone, String type);

    /**
     * 校验验证码
     */
    boolean verifyCode(String phone, String code, String type);
}