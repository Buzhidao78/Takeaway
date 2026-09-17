package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.StoreMapper;
import com.bzy.takeaway.mapper.SysUserMapper;
import com.bzy.takeaway.service.AuthService;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.SmsService;
import com.bzy.takeaway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final StoreMapper storeMapper;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 登录失败最大次数
    private static final int MAX_LOGIN_FAILURES = 5;
    // 锁定时间（分钟）
    private static final int LOCK_TIME_MINUTES = 30;
    // 验证码有效期（分钟）
    private static final int CAPTCHA_EXPIRE_MINUTES = 10;

    @Override
    public Result<SysUser> login(String phone, String password, String captcha) {
        // 检查是否需要验证码（失败次数达到 5 次）
        String failureKey = "login:failure:" + phone;
        String lockKey = "login:lock:" + phone;
        
        // 检查账号是否被锁定
        Boolean isLocked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(isLocked)) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            return Result.fail("账号已被锁定，请" + ttl + "分钟后再试");
        }
        
        // 获取当前失败次数（不递增，只是查询）
        String failureCountStr = redisTemplate.opsForValue().get(failureKey);
        Long failureCount = failureCountStr != null ? Long.parseLong(failureCountStr) : 0L;
        
        System.out.println("=== 登录请求 [" + phone + "] 当前失败次数：" + failureCount + " (Redis: " + failureCountStr + ") ===");
        
        boolean needCaptcha = failureCount >= MAX_LOGIN_FAILURES;
        
        // 如果需要验证码，先验证
        if (needCaptcha) {
            if (captcha == null || captcha.trim().isEmpty()) {
                return Result.fail("请输入验证码");
            }
            String savedCaptcha = redisTemplate.opsForValue().get("captcha:" + phone);
            if (savedCaptcha == null || !savedCaptcha.equalsIgnoreCase(captcha)) {
                // 验证码错误，增加失败次数
                redisTemplate.opsForValue().increment(failureKey);
                redisTemplate.expire(failureKey, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
                System.out.println("=== 验证码错误 [" + phone + "] 新失败次数：" + (failureCount + 1) + " ===");
                return Result.fail("验证码错误");
            }
        }
        
        // 查询用户
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        
        // 方案 1：模糊错误提示 - 不泄露手机号是否注册
        if (u == null || u.getStatus() != 1 || !encoder.matches(password, u.getPassword())) {
            // 增加失败次数
            Long newCount = redisTemplate.opsForValue().increment(failureKey);
            redisTemplate.expire(failureKey, LOCK_TIME_MINUTES, TimeUnit.MINUTES);
            
            System.out.println("=== 登录失败 [" + phone + "] 新失败次数：" + newCount + " ===");
            
            // 如果达到最大失败次数，锁定账号
            if (failureCount >= MAX_LOGIN_FAILURES) {
                redisTemplate.opsForValue().set(lockKey, "locked", LOCK_TIME_MINUTES, TimeUnit.MINUTES);
                return Result.fail("账号已被锁定，请" + LOCK_TIME_MINUTES + "分钟后再试");
            }
            
            // 返回模糊错误信息
            return Result.fail("手机号或密码错误");
        }
        
        // 登录成功，清除失败记录
        redisTemplate.delete(failureKey);
        redisTemplate.delete(lockKey);
        
        System.out.println("=== 登录成功 [" + phone + "] 已清除失败记录 ===");
        
        u.setPassword(null);
        u.setToken(jwtUtil.generateToken(u.getId(), u.getPhone(), u.getRole()));
        return Result.ok(u);
    }

    /**
     * 生成登录验证码
     */
    @Override
    public Result<String> generateLoginCaptcha(String phone) {
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) {
            return Result.fail("手机号格式错误");
        }
        
        // 生成 6 位随机验证码
        String captcha = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        
        // 保存到 Redis，有效期 10 分钟
        redisTemplate.opsForValue().set("captcha:" + phone, captcha, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 实际项目中这里应该调用短信服务发送验证码
        // 测试环境直接返回验证码
        System.out.println("=== 登录验证码 [" + phone + "]: " + captcha + " ===");
        
        return Result.ok("验证码已发送");
    }

    /**
     * 获取登录失败次数
     */
    @Override
    public Result<Integer> getLoginFailureCount(String phone) {
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) {
            return Result.fail("手机号格式错误");
        }
        
        String failureKey = "login:failure:" + phone;
        String countStr = redisTemplate.opsForValue().get(failureKey);
        Integer count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        System.out.println("=== 查询失败次数 [" + phone + "]: " + count + " (Redis: " + countStr + ") ===");
        
        return Result.ok(count);
    }

    @Override
    public SysUser getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 重置密码
     */
    @Override
    public Result<String> resetPassword(String phone, String password, String code) {
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) {
            return Result.fail("手机号格式错误");
        }
        
        if (password == null || password.length() < 6) {
            return Result.fail("密码至少 6 位");
        }
        
        // 验证验证码
        if (!smsService.verifyCode(phone, code, "resetPassword")) {
            return Result.fail("验证码错误或已过期");
        }
        
        // 查询用户
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone));
        
        if (user == null) {
            return Result.fail("手机号未注册");
        }
        
        if (user.getStatus() != 1) {
            return Result.fail("账号已被禁用");
        }
        
        // 更新密码
        user.setPassword(encoder.encode(password));
        userMapper.updateById(user);
        
        // 清除登录失败记录
        String failureKey = "login:failure:" + phone;
        String lockKey = "login:lock:" + phone;
        redisTemplate.delete(failureKey);
        redisTemplate.delete(lockKey);
        
        System.out.println("=== 密码重置成功 [" + phone + "] ===");
        
        return Result.ok("密码重置成功");
    }

    @Override
    public Result<String> sendCode(String phone, String type) {
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) return Result.fail("手机号格式错误");
        smsService.sendCode(phone, type);
        return Result.ok("验证码已发送");
    }

    @Override
    public Result<SysUser> register(String phone, String password, String code, String nickname) {
        System.out.println("=== 开始注册用户，手机号：" + phone + " ===");
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) return Result.fail("手机号格式错误");
        if (password == null || password.length() < 6) return Result.fail("密码至少 6 位");
        if (!smsService.verifyCode(phone, code, "register")) return Result.fail("验证码错误或已过期");

        // 检查手机号是否已被未删除的用户使用
        SysUser exist = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhone, phone)
            .eq(SysUser::getDeleted, 0));
        if (exist != null) {
            System.out.println(">>> 手机号已被未删除用户使用：" + exist.getId());
            return Result.fail("该手机号已注册");
        }
        System.out.println(">>> 手机号未被未删除用户使用");

        // 检查是否存在已注销的用户，如果存在，先物理删除
        SysUser deletedUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhone, phone)
            .eq(SysUser::getDeleted, 1));
        if (deletedUser != null) {
            System.out.println(">>> 检测到已注销用户 ID=" + deletedUser.getId() + "，准备物理删除");
            // 使用物理删除方法
            userMapper.deletePhysical(deletedUser.getId());
            System.out.println(">>> 物理删除结果完成");
        } else {
            System.out.println(">>> 未检测到已注销用户");
        }

        SysUser u = new SysUser();
        u.setPhone(phone);
        u.setPassword(encoder.encode(password));
        u.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "用户" + phone.substring(7));
        u.setRole(Constants.ROLE_USER);
        u.setStatus(1);
        System.out.println(">>> 准备插入新用户");
        userMapper.insert(u);
        System.out.println(">>> 新用户 ID=" + u.getId());
        System.out.println("=== 用户注册成功 ===");
        u.setPassword(null);
        u.setToken(jwtUtil.generateToken(u.getId(), u.getPhone(), u.getRole()));
        return Result.ok(u);
    }

    @Override
    @Transactional
    public Result<SysUser> merchantRegister(String phone, String password, String code, 
                                            String storeName, String storeAddress, String storePhone) {
        if (phone == null || !phone.matches("1[3-9]\\d{9}")) return Result.fail("手机号格式错误");
        if (password == null || password.length() < 6) return Result.fail("密码至少 6 位");
        if (!smsService.verifyCode(phone, code, "register")) return Result.fail("验证码错误或已过期");

        // 检查手机号是否已被未删除的用户使用
        SysUser exist = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhone, phone)
            .eq(SysUser::getDeleted, 0));
        if (exist != null) return Result.fail("该手机号已注册");

        // 检查是否存在已注销的用户，如果存在，先物理删除
        SysUser deletedUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhone, phone)
            .eq(SysUser::getDeleted, 1));
        if (deletedUser != null) {
            // 使用 delete 方法进行物理删除
            userMapper.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, deletedUser.getId()));
        }

        SysUser u = new SysUser();
        u.setPhone(phone);
        u.setPassword(encoder.encode(password));
        u.setNickname(storeName != null && !storeName.isEmpty() ? storeName : "商家" + phone.substring(7));
        u.setRole(Constants.ROLE_MERCHANT);
        u.setStatus(1);
        userMapper.insert(u);

        Store store = new Store();
        store.setUserId(u.getId());
        store.setName(storeName != null && !storeName.isEmpty() ? storeName : "店铺" + phone.substring(7));
        store.setAddress(storeAddress);
        store.setPhone(storePhone != null && !storePhone.isEmpty() ? storePhone : phone);
        store.setStatus(Constants.STORE_STATUS_CLOSED);
        store.setAuditStatus(0);
        store.setSalesCount(0);
        store.setRating(new java.math.BigDecimal("0.0"));
        storeMapper.insert(store);

        // 发送通知给所有管理员
        try {
            List<SysUser> admins = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Constants.ROLE_ADMIN)
                .eq(SysUser::getDeleted, 0));
            for (SysUser admin : admins) {
                notificationService.sendNotification(admin.getId(), "商家注册申请",
                    String.format("商家「%s」（%s）提交注册申请，请及时审核", store.getName(), phone),
                    "audit", u.getId());
            }
            log.info("已向 {} 位管理员发送商家注册通知", admins.size());
        } catch (Exception e) {
            log.warn("发送商家注册通知失败: {}", e.getMessage());
        }

        u.setPassword(null);
        u.setToken(jwtUtil.generateToken(u.getId(), u.getPhone(), u.getRole()));
        return Result.ok(u);
    }
}
