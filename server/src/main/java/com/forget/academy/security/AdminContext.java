package com.forget.academy.security;

import com.forget.academy.entity.AdminUser;

public final class AdminContext {
    private static final ThreadLocal<AdminUser> HOLDER = new ThreadLocal<>();

    private AdminContext() {
    }

    public static void set(AdminUser admin) {
        HOLDER.set(admin);
    }

    public static AdminUser get() {
        return HOLDER.get();
    }

    public static AdminUser require() {
        AdminUser admin = HOLDER.get();
        if (admin == null) {
            throw new com.forget.academy.common.BizException(401, "请先登录管理后台");
        }
        return admin;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
