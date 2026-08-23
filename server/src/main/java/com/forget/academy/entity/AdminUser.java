package com.forget.academy.entity;

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
    private String role = "ADMIN";
}
