package com.forget.academy.common;

public final class AdminRoles {
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String PRINCIPAL = "PRINCIPAL";
    /** 兼容旧数据 */
    public static final String LEGACY_ADMIN = "ADMIN";

    private AdminRoles() {
    }

    public static boolean isSuperAdmin(String role) {
        return SUPER_ADMIN.equals(role) || LEGACY_ADMIN.equals(role) || role == null || role.isBlank();
    }
}
