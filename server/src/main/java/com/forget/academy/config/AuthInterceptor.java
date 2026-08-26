package com.forget.academy.config;

import com.forget.academy.common.BizException;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.security.AdminContext;
import com.forget.academy.security.AuthContext;
import com.forget.academy.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final AppUserRepo appUserRepo;
    private final AdminUserRepo adminUserRepo;

    @Value("${app.wx-appid:}")
    private String wxAppid;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        parseToken(request);

        if (path.startsWith("/api/admin") && !path.equals("/api/admin/auth/login")) {
            AuthContext.requireAdmin();
            adminUserRepo.findById(AuthContext.requireAdmin().id()).ifPresent(AdminContext::set);
        }
        if (requiresAppAuth(path, request.getMethod())) {
            AuthContext.requireApp();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
        AdminContext.clear();
    }

    private void parseToken(HttpServletRequest request) {
        String bearer = extractBearer(request);
        if (bearer != null) {
            try {
                Claims claims = jwtUtil.parse(bearer);
                Long id = Long.parseLong(claims.getSubject());
                String role = String.valueOf(claims.get("role"));
                String name = claims.get("name") == null ? "" : String.valueOf(claims.get("name"));
                AuthContext.set(new AuthContext.AuthUser(id, role, name));
                return;
            } catch (Exception ignored) {
                // 微信云托管可能覆盖 Authorization，继续用其它方式识别学员
            }
        }
        if (tryBindWxOpenid(request)) {
            return;
        }
        String path = request.getRequestURI();
        if (bearer != null && requiresAuth(path, request.getMethod())) {
            throw new BizException(401, "登录已过期，请重新登录");
        }
    }

    private boolean tryBindWxOpenid(HttpServletRequest request) {
        String openid = firstHeader(request, "X-WX-OPENID", "x-wx-openid");
        if (openid == null || openid.isBlank()) {
            return false;
        }
        String appid = firstHeader(request, "X-WX-APPID", "x-wx-appid");
        if (wxAppid != null && !wxAppid.isBlank() && appid != null && !appid.isBlank() && !wxAppid.equals(appid)) {
            return false;
        }
        return appUserRepo.findByOpenid(openid).map(user -> {
            AuthContext.set(new AuthContext.AuthUser(
                    user.getId(),
                    AuthContext.ROLE_APP,
                    user.getNickname() == null ? "" : user.getNickname()));
            return true;
        }).orElse(false);
    }

    private static String extractBearer(HttpServletRequest request) {
        String[] keys = {"X-App-Token", "X-Token", "Authorization"};
        for (String key : keys) {
            String value = request.getHeader(key);
            if (value == null || value.isBlank()) {
                continue;
            }
            value = value.trim();
            if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
                value = value.substring(7).trim();
            }
            if (!value.isBlank() && value.contains(".")) {
                return value;
            }
        }
        return null;
    }

    private static String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
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
                || path.startsWith("/api/app/upload")
                || path.startsWith("/api/app/mine")
                || path.startsWith("/api/app/bookings")
                || path.startsWith("/api/app/waitlist")
                || path.startsWith("/api/app/teacher/")
                || path.startsWith("/api/app/teacher-reviews")
                || path.startsWith("/api/app/employee")
                || path.startsWith("/api/app/cards")
                || path.startsWith("/api/app/my-courses")
                || path.startsWith("/api/app/practice")
                || path.startsWith("/api/app/checkin")
                || path.startsWith("/api/app/applies")
                || path.startsWith("/api/app/feedback")
                || ("POST".equalsIgnoreCase(method) && path.startsWith("/api/app/opportunities"));
    }
}
