package com.forget.academy.security;

public class AuthContext {
    public static final String ROLE_APP = "APP";
    public static final String ROLE_ADMIN = "ADMIN";

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    public static AuthUser requireApp() {
        AuthUser user = HOLDER.get();
        if (user == null || !ROLE_APP.equals(user.role())) {
            throw new com.forget.academy.common.BizException(401, "请先登录");
        }
        return user;
    }

    public static AuthUser requireAdmin() {
        AuthUser user = HOLDER.get();
        if (user == null || !ROLE_ADMIN.equals(user.role())) {
            throw new com.forget.academy.common.BizException(401, "请先登录管理后台");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record AuthUser(Long id, String role, String name) {}
}
