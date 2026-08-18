package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody Map<String, String> body) {
        return authService.login(
            body.get("phone"), 
            body.get("password"),
            body.get("captcha")
        );
    }

    /**
     * 获取登录验证码
     */
    @PostMapping("/captcha")
    public Result<String> getCaptcha(@RequestBody Map<String, String> body) {
        return authService.generateLoginCaptcha(body.get("phone"));
    }

    /**
     * 获取登录失败次数
     */
    @GetMapping("/failureCount")
    public Result<Integer> getFailureCount(@RequestParam String phone) {
        return authService.getLoginFailureCount(phone);
    }

    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestBody Map<String, String> body) {
        return authService.sendCode(body.get("phone"), body.getOrDefault("type", "register"));
    }

    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody Map<String, String> body) {
        return authService.register(
                body.get("phone"),
                body.get("password"),
                body.get("code"),
                body.get("nickname")
        );
    }

    @GetMapping("/profile")
    public Result<SysUser> profile(@RequestAttribute("userId") Long userId) {
        SysUser user = authService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.ok(user);
        }
        return Result.fail("用户不存在");
    }

    @PostMapping("/merchant/register")
    public Result<SysUser> merchantRegister(@RequestBody Map<String, String> body) {
        return authService.merchantRegister(
                body.get("phone"),
                body.get("password"),
                body.get("code"),
                body.get("storeName"),
                body.get("storeAddress"),
                body.get("storePhone")
        );
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        return authService.resetPassword(
                body.get("phone"),
                body.get("password"),
                body.get("code")
        );
    }
}
