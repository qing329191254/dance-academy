package com.forget.academy.entity;

import com.forget.academy.common.AdminRoles;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "admin_user")
public class AdminUser extends BaseEntity {
    @Column(unique = true, nullable = false, length = 64)
    private String username;
    private String passwordHash;
    private String name;
    /** SUPER_ADMIN / PRINCIPAL，兼容旧值 ADMIN */
    private String role = AdminRoles.SUPER_ADMIN;
    /** 校长可管理的校区，逗号分隔；超级管理员为空表示全部 */
    @Column(length = 512)
    private String campusIds;
}
