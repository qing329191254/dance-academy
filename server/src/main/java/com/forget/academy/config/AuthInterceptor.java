package com.forget.academy.config;

import com.forget.academy.common.BizException;
import com.forget.academy.security.AuthContext;
import com.forget.academy.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        parseToken(request);

        if (path.startsWith("/api/admin") && !path.equals("/api/admin/auth/login")) {
            AuthContext.requireAdmin();
        }
        if (requiresAppAuth(path, request.getMethod())) {
            AuthContext.requireApp();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void parseToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        try {
            Claims claims = jwtUtil.parse(header.substring(7));
            Long id = Long.parseLong(claims.getSubject());
            String role = String.valueOf(claims.get("role"));
            String name = claims.get("name") == null ? "" : String.valueOf(claims.get("name"));
            AuthContext.set(new AuthContext.AuthUser(id, role, name));
        } catch (Exception e) {
            String path = request.getRequestURI();
            if (requiresAuth(path, request.getMethod())) {
                throw new BizException(401, "登录已过期，请重新登录");
            }
        }
    }

    private boolean requiresAuth(String path, String method) {
        if (path.startsWith("/api/admin") && !path.equals("/api/admin/auth/login")) {
            return true;
        }
        return requiresAppAuth(path, method);
    }

    private boolean requiresAppAuth(String path, String method) {
        if (!path.startsWith("/api/app/")) {
            return false;
        }
        if (path.equals("/api/app/auth/login")) {
            return false;
        }
        return path.startsWith("/api/app/auth/profile")
                || path.startsWith("/api/app/mine")
                || path.startsWith("/api/app/bookings")
                || path.startsWith("/api/app/cards")
                || path.startsWith("/api/app/my-courses")
                || path.startsWith("/api/app/practice")
                || path.startsWith("/api/app/checkin")
                || path.startsWith("/api/app/applies")
                || ("POST".equalsIgnoreCase(method) && path.startsWith("/api/app/opportunities"));
    }
}
