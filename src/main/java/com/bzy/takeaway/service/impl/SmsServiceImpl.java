package com.bzy.takeaway.service.impl;

import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final StringRedisTemplate redisTemplate;

    @Value("${sms.dev-mode:true}")
    private boolean devMode;

    @Override
    public void sendCode(String phone, String type) {
        String code = generateCode();
        String key = Constants.REDIS_SMS_PREFIX + type + ":" + phone;
        redisTemplate.opsForValue().set(key, code, Constants.SMS_EXPIRE_MINUTES, TimeUnit.MINUTES);

        if (devMode) {
            log.info("【BZY外卖-验证码】手机号:{} 验证码:{} (开发模式，5分钟有效)", phone, code);
        } else {
            // 接入阿里云短信
            sendByAliyun(phone, code, type);
        }
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        if (devMode && "123456".equals(code)) {
            return true; // 开发模式万能码
        }
        String key = Constants.REDIS_SMS_PREFIX + type + ":" + phone;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null && cached.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.SMS_CODE_LENGTH; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    private void sendByAliyun(String phone, String code, String type) {
        // 阿里云短信发送逻辑 - 需配置 accessKeyId, accessKeySecret, signName, templateCode
        log.warn("阿里云短信未配置，请设置 sms.access-key-id 等参数");
    }
}