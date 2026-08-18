package com.bzy.takeaway.interceptor;

import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RiderAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;

        String auth = request.getHeader(Constants.TOKEN_HEADER);
        if (auth == null || !auth.startsWith(Constants.TOKEN_PREFIX)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
            return false;
        }
        String token = auth.substring(Constants.TOKEN_PREFIX.length());
        Long riderId = jwtUtil.getUserId(token);
        if (riderId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效\"}");
            return false;
        }
        request.setAttribute("userId", riderId);
        request.setAttribute("riderId", riderId);
        request.setAttribute("token", token);
        return true;
    }
}
