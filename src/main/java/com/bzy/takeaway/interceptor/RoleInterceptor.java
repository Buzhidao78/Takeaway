package com.bzy.takeaway.interceptor;

import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {

    private final SysUserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return false;

        SysUser u = userMapper.selectById(userId);
        if (u == null) return false;

        String path = request.getRequestURI();
        if (path.contains("/admin/")) {
            if (u.getRole() != Constants.ROLE_ADMIN) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"需要管理员权限\"}");
                return false;
            }
        }
        if (path.contains("/merchant/")) {
            if (u.getRole() != Constants.ROLE_MERCHANT && u.getRole() != Constants.ROLE_ADMIN) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"需要商家权限\"}");
                return false;
            }
        }
        return true;
    }
}
