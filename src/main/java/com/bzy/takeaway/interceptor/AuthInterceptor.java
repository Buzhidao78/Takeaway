package com.bzy.takeaway.interceptor;

import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.SysUserMapper;
import com.bzy.takeaway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;

        String auth = request.getHeader(Constants.TOKEN_HEADER);
        System.out.println("=== AuthInterceptor: Authorization header = " + auth + " ===");
        
        if (auth == null || !auth.startsWith(Constants.TOKEN_PREFIX)) {
            System.out.println("=== AuthInterceptor: token 格式错误 ===");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或 token 已过期\"}");
            return false;
        }
        String token = auth.substring(Constants.TOKEN_PREFIX.length());
        System.out.println("=== AuthInterceptor: 提取的 token = " + token + " ===");
        
        Long userId = jwtUtil.getUserId(token);
        System.out.println("=== AuthInterceptor: 解析的 userId = " + userId + " ===");
        
        if (userId == null) {
            System.out.println("=== AuthInterceptor: token 无效，userId 为 null ===");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token 无效\"}");
            return false;
        }
        
        SysUser user = userMapper.selectById(userId);
        System.out.println("=== AuthInterceptor: 查询的用户 = " + (user != null ? user.getPhone() : "null") + " ===");
        
        if (user == null || user.getDeleted() == 1 || user.getStatus() != 1) {
            System.out.println("=== AuthInterceptor: 账号已注销或禁用 ===");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"账号已注销或禁用\"}");
            return false;
        }
        
        request.setAttribute("userId", userId);
        request.setAttribute("token", token);
        System.out.println("=== AuthInterceptor: 设置 userId 成功 = " + userId + " ===");
        return true;
    }
}
